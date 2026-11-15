package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BizSphereDatabase
import com.example.data.local.BusinessTransaction
import com.example.data.local.CashbookType
import com.example.data.local.Customer
import com.example.data.local.CustomerDao
import com.example.data.local.KhataType
import com.example.data.local.Ledger
import com.example.data.local.LedgerDao
import com.example.data.local.PaymentMethod
import com.example.data.local.TransactionDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BizSphereRoomDaoTest {

    private lateinit var db: BizSphereDatabase
    private lateinit var customerDao: CustomerDao
    private lateinit var ledgerDao: LedgerDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BizSphereDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        customerDao = db.customerDao()
        ledgerDao = db.ledgerDao()
        transactionDao = db.transactionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun customerCrudAndSearchWorkCorrectly() = runTest {
        val id = customerDao.insertCustomer(
            Customer(name = "Rahim Enterprise", phone = "01711000000", address = "Dhaka")
        )
        val loaded = customerDao.getCustomerByIdOnce(id)
        assertNotNull(loaded)
        assertEquals("Rahim Enterprise", loaded?.name)

        customerDao.updateCustomer(loaded!!.copy(phone = "01811000000"))
        val searched = customerDao.searchCustomers("01811").first()
        assertEquals(1, searched.size)
        assertEquals("01811000000", searched.first().phone)

        customerDao.deleteCustomerById(id)
        assertNull(customerDao.getCustomerByIdOnce(id))
    }

    @Test
    fun ledgerCrudAndCascadeDeleteWorkCorrectly() = runTest {
        val customerId = customerDao.insertCustomer(Customer(name = "Karim Store"))
        val givenId = ledgerDao.insertEntry(
            Ledger(
                customerId = customerId,
                type = KhataType.CREDIT_GIVEN,
                amount = 2500.0,
                description = "Goods credit"
            )
        )
        ledgerDao.insertEntry(
            Ledger(
                customerId = customerId,
                type = KhataType.PAYMENT_RECEIVED,
                amount = 1000.0,
                paymentMethod = PaymentMethod.BKASH
            )
        )

        val customerWithLedger = customerDao.observeCustomerWithLedger(customerId).first()
        assertNotNull(customerWithLedger)
        assertEquals(2, customerWithLedger!!.ledgerEntries.size)

        val entry = ledgerDao.getEntryByIdOnce(givenId)!!
        ledgerDao.updateEntry(entry.copy(amount = 3000.0))
        assertEquals(3000.0, ledgerDao.getEntryByIdOnce(givenId)?.amount ?: 0.0, 0.01)

        // Deleting customer cascades to ledger entries
        customerDao.deleteCustomerById(customerId)
        assertEquals(0, ledgerDao.observeEntriesForCustomer(customerId).first().size)
    }

    @Test
    fun transactionCrudAndTypeFilteringWorkCorrectly() = runTest {
        val incomeId = transactionDao.insertEntry(
            BusinessTransaction(
                type = CashbookType.INCOME,
                category = "Sales",
                amount = 5000.0,
                description = "Counter sales"
            )
        )
        transactionDao.insertEntry(
            BusinessTransaction(
                type = CashbookType.EXPENSE,
                category = "Rent",
                amount = 1500.0,
                description = "Shop rent"
            )
        )

        val incomes = transactionDao.observeEntriesByType(CashbookType.INCOME).first()
        assertEquals(1, incomes.size)
        assertEquals(5000.0, incomes.first().amount, 0.01)

        val updated = transactionDao.getEntryByIdOnce(incomeId)!!.copy(amount = 5500.0)
        transactionDao.updateEntry(updated)
        assertEquals(5500.0, transactionDao.getEntryByIdOnce(incomeId)?.amount ?: 0.0, 0.01)

        transactionDao.deleteEntryById(incomeId)
        assertNull(transactionDao.getEntryByIdOnce(incomeId))
    }
}
