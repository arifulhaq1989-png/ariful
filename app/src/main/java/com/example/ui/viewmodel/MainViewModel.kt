package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.core.SecurityHasher
import com.example.core.ThemeMode
import com.example.data.local.BizSphereDatabase
import com.example.data.local.CashbookEntryEntity
import com.example.data.local.CashbookType
import com.example.data.local.CustomerEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.KhataEntryEntity
import com.example.data.local.KhataType
import com.example.data.prefs.UserPreferences
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.repository.BizSphereRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class CustomerSummary(
    val customer: CustomerEntity,
    val totalGiven: Double,
    val totalReceived: Double,
    val netBalance: Double, // > 0 = Customer owes you (Due), < 0 = You owe customer (Payable)
    val entryCount: Int
)

enum class UnifiedTxKind {
    KHATA_GIVEN,
    KHATA_RECEIVED,
    INCOME,
    EXPENSE,
    INVOICE
}

data class UnifiedTransaction(
    val id: String,
    val kind: UnifiedTxKind,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val timestamp: Long,
    val paymentMethod: String,
    val customerId: Long? = null
)

data class DailyChartPoint(
    val timestamp: Long,
    val income: Double,
    val expense: Double
)

data class MainUiState(
    val isLoading: Boolean = true,
    val prefs: UserPreferences = UserPreferences(),
    val isPinUnlocked: Boolean = false,
    val customers: List<CustomerSummary> = emptyList(),
    val khataEntries: List<KhataEntryEntity> = emptyList(),
    val cashbookEntries: List<CashbookEntryEntity> = emptyList(),
    val invoices: List<InvoiceWithItems> = emptyList(),
    val unifiedTransactions: List<UnifiedTransaction> = emptyList(),
    val totalSales: Double = 0.0,
    val totalDueReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val totalCashIncome: Double = 0.0,
    val totalCashExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val last7DaysChart: List<DailyChartPoint> = emptyList(),
    val dueCustomersCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = UserPreferencesRepository(application)
    private val db = BizSphereDatabase.getInstance(application)
    val repository = BizSphereRepository(db)

    private val _isPinUnlocked = MutableStateFlow(false)

    val uiState: StateFlow<MainUiState> = combine(
        prefsRepo.preferencesFlow,
        repository.customersFlow,
        repository.khataEntriesFlow,
        repository.cashbookEntriesFlow,
        repository.invoicesFlow,
        _isPinUnlocked
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val prefs = values[0] as UserPreferences
        @Suppress("UNCHECKED_CAST")
        val customers = values[1] as List<CustomerEntity>
        @Suppress("UNCHECKED_CAST")
        val khata = values[2] as List<KhataEntryEntity>
        @Suppress("UNCHECKED_CAST")
        val cashbook = values[3] as List<CashbookEntryEntity>
        @Suppress("UNCHECKED_CAST")
        val invoices = values[4] as List<InvoiceWithItems>
        val pinUnlocked = values[5] as Boolean

        val customerMap = customers.associateBy { it.id }
        val khataByCustomer = khata.groupBy { it.customerId }

        val customerSummaries = customers.map { cust ->
            val entries = khataByCustomer[cust.id].orEmpty()
            val given = entries.filter { it.type == KhataType.CREDIT_GIVEN }.sumOf { it.amount }
            val received = entries.filter { it.type == KhataType.PAYMENT_RECEIVED }.sumOf { it.amount }
            CustomerSummary(
                customer = cust,
                totalGiven = given,
                totalReceived = received,
                netBalance = given - received,
                entryCount = entries.size
            )
        }

        val totalDueReceivable = customerSummaries.filter { it.netBalance > 0.01 }.sumOf { it.netBalance }
        val totalPayable = customerSummaries.filter { it.netBalance < -0.01 }.sumOf { -it.netBalance }
        val dueCustomersCount = customerSummaries.count { it.netBalance > 0.01 }

        val totalCashIncome = cashbook.filter { it.type == CashbookType.INCOME }.sumOf { it.amount }
        val totalCashExpense = cashbook.filter { it.type == CashbookType.EXPENSE }.sumOf { it.amount }

        val invoiceSalesTotal = invoices.sumOf { it.invoice.totalAmount }
        val standaloneSales = cashbook
            .filter { it.type == CashbookType.INCOME && it.category == "Sales" && !it.description.startsWith("INV-") }
            .sumOf { it.amount }
        val otherIncome = cashbook
            .filter { it.type == CashbookType.INCOME && it.category != "Sales" }
            .sumOf { it.amount }

        val totalSales = invoiceSalesTotal + standaloneSales
        val netProfit = (totalSales + otherIncome) - totalCashExpense

        val unified = buildList {
            khata.forEach { k ->
                val custName = customerMap[k.customerId]?.name ?: "Customer"
                add(
                    UnifiedTransaction(
                        id = "k_${k.id}",
                        kind = if (k.type == KhataType.CREDIT_GIVEN) UnifiedTxKind.KHATA_GIVEN else UnifiedTxKind.KHATA_RECEIVED,
                        title = custName,
                        subtitle = k.description.ifBlank { k.paymentMethod },
                        amount = k.amount,
                        timestamp = k.timestamp,
                        paymentMethod = k.paymentMethod,
                        customerId = k.customerId
                    )
                )
            }
            cashbook.forEach { cb ->
                add(
                    UnifiedTransaction(
                        id = "c_${cb.id}",
                        kind = if (cb.type == CashbookType.INCOME) UnifiedTxKind.INCOME else UnifiedTxKind.EXPENSE,
                        title = cb.category,
                        subtitle = cb.description.ifBlank { cb.paymentMethod },
                        amount = cb.amount,
                        timestamp = cb.timestamp,
                        paymentMethod = cb.paymentMethod
                    )
                )
            }
            invoices.forEach { inv ->
                add(
                    UnifiedTransaction(
                        id = "i_${inv.invoice.id}",
                        kind = UnifiedTxKind.INVOICE,
                        title = "${inv.invoice.invoiceNumber} • ${inv.invoice.customerName}",
                        subtitle = inv.invoice.notes.ifBlank { "${inv.items.size} items" },
                        amount = inv.invoice.totalAmount,
                        timestamp = inv.invoice.timestamp,
                        paymentMethod = "Invoice",
                        customerId = inv.invoice.customerId
                    )
                )
            }
        }.sortedByDescending { it.timestamp }

        val chartPoints = buildLast7DaysChart(cashbook, invoices)

        MainUiState(
            isLoading = false,
            prefs = prefs,
            isPinUnlocked = if (!prefs.isPinEnabled) true else pinUnlocked,
            customers = customerSummaries,
            khataEntries = khata,
            cashbookEntries = cashbook,
            invoices = invoices,
            unifiedTransactions = unified,
            totalSales = totalSales,
            totalDueReceivable = totalDueReceivable,
            totalPayable = totalPayable,
            totalCashIncome = totalCashIncome,
            totalCashExpense = totalCashExpense,
            netProfit = netProfit,
            last7DaysChart = chartPoints,
            dueCustomersCount = dueCustomersCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MainUiState()
    )

    private fun buildLast7DaysChart(
        cashbook: List<CashbookEntryEntity>,
        invoices: List<InvoiceWithItems>
    ): List<DailyChartPoint> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis
        val dayMs = 86_400_000L

        return (6 downTo 0).map { offset ->
            val start = todayStart - offset * dayMs
            val end = start + dayMs
            val dayIncome = cashbook
                .filter { it.type == CashbookType.INCOME && it.timestamp in start until end }
                .sumOf { it.amount } +
                invoices.filter { it.invoice.timestamp in start until end }
                    .sumOf { it.invoice.balanceDue }
            val dayExpense = cashbook
                .filter { it.type == CashbookType.EXPENSE && it.timestamp in start until end }
                .sumOf { it.amount }
            DailyChartPoint(start, dayIncome, dayExpense)
        }
    }

    // Auth & Security
    fun registerAccount(
        ownerName: String,
        businessName: String,
        emailOrPhone: String,
        password: String
    ) {
        viewModelScope.launch {
            prefsRepo.registerAccount(ownerName, businessName, emailOrPhone, password)
            _isPinUnlocked.value = true
        }
    }

    fun loginWithCredentials(emailOrPhone: String, password: String): Boolean {
        val prefs = uiState.value.prefs
        val inputHash = SecurityHasher.sha256(password)
        val matches = prefs.isRegistered &&
            prefs.accountEmail.equals(emailOrPhone.trim(), ignoreCase = true) &&
            prefs.accountPasswordHash == inputHash
        if (matches) {
            viewModelScope.launch {
                prefsRepo.setLoggedIn(true)
                _isPinUnlocked.value = true
            }
        }
        return matches
    }

    fun continueQuickStart() {
        viewModelScope.launch {
            prefsRepo.continueQuickStart()
            _isPinUnlocked.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefsRepo.setLoggedIn(false)
            _isPinUnlocked.value = false
        }
    }

    fun verifyPin(pin: String): Boolean {
        val prefs = uiState.value.prefs
        val ok = SecurityHasher.sha256(pin) == prefs.pinHash
        if (ok) _isPinUnlocked.value = true
        return ok
    }

    fun unlockWithPasswordAndDisablePin(password: String): Boolean {
        val prefs = uiState.value.prefs
        if (prefs.isRegistered && SecurityHasher.sha256(password) == prefs.accountPasswordHash) {
            viewModelScope.launch {
                prefsRepo.disablePin()
                _isPinUnlocked.value = true
            }
            return true
        }
        return false
    }

    fun lockAppNow() {
        if (uiState.value.prefs.isPinEnabled) {
            _isPinUnlocked.value = false
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            prefsRepo.setPin(pin)
            _isPinUnlocked.value = true
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            prefsRepo.disablePin()
            _isPinUnlocked.value = true
        }
    }

    // Preferences
    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch { prefsRepo.setCurrencySymbol(symbol) }
    }

    fun updateBusinessProfile(name: String, owner: String, phone: String, address: String) {
        viewModelScope.launch { prefsRepo.updateBusinessProfile(name, owner, phone, address) }
    }

    // Customers & Khata
    fun saveCustomer(customer: CustomerEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.upsertCustomer(customer)
            onDone(id)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }

    fun updateCustomerDueReminder(customerId: Long, dueDate: Long?) {
        viewModelScope.launch { repository.updateCustomerDueReminder(customerId, dueDate) }
    }

    fun saveKhataEntry(entry: KhataEntryEntity) {
        viewModelScope.launch { repository.upsertKhataEntry(entry) }
    }

    fun deleteKhataEntry(entry: KhataEntryEntity) {
        viewModelScope.launch { repository.deleteKhataEntry(entry) }
    }

    // Cashbook
    fun saveCashbookEntry(entry: CashbookEntryEntity) {
        viewModelScope.launch { repository.upsertCashbookEntry(entry) }
    }

    fun deleteCashbookEntry(entry: CashbookEntryEntity) {
        viewModelScope.launch { repository.deleteCashbookEntry(entry) }
    }

    // Invoices
    suspend fun getNextInvoiceNumber(): String = repository.generateNextInvoiceNumber()

    fun saveInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        syncToKhataAndSales: Boolean,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.saveInvoice(invoice, items, syncToKhataAndSales)
            onSaved(id)
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch { repository.deleteInvoice(invoice) }
    }

    // Backup & Restore
    fun exportBackupToUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val json = repository.exportBackupJson(uiState.value.prefs)
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    prefsRepo.setLastBackupAt(System.currentTimeMillis())
                    true
                } catch (_: Exception) {
                    false
                }
            }
            onResult(success)
        }
    }

    fun restoreBackupFromUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader().readText()
                    } ?: return@withContext false
                    val payload = repository.importBackupJson(json)
                    prefsRepo.updateBusinessProfile(
                        payload.businessName,
                        payload.ownerName,
                        payload.businessPhone,
                        payload.businessAddress
                    )
                    prefsRepo.setCurrencySymbol(payload.currencySymbol)
                    true
                } catch (_: Exception) {
                    false
                }
            }
            onResult(success)
        }
    }

    fun seedDemoData() {
        viewModelScope.launch { repository.seedDemoData() }
    }

    fun clearAllData() {
        viewModelScope.launch { repository.clearAllData() }
    }
}
