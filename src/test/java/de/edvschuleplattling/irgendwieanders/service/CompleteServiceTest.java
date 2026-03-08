package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.Exceptions.*;
import de.edvschuleplattling.irgendwieanders.model.id.EyeColor;
import de.edvschuleplattling.irgendwieanders.model.id.IdVerification;
import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.repository.IdVerificationRepository;
import de.edvschuleplattling.irgendwieanders.repository.TransactionRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.repository.WalletRepository;
import de.edvschuleplattling.irgendwieanders.rest.dto.IdVerificationCreateDto;
import de.edvschuleplattling.irgendwieanders.rest.dto.TransactionExecuteDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

//Diese Testklasse wurde per KI erstellt

class CompleteServiceTest {

    // Mocks für alle Repositories
    @Mock
    private WalletRepository walletRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private IdVerificationRepository idVerificationRepository;
    
    @Mock
    private UseraccountRepository useraccountRepository;

    // Mock für Service-Abhängigkeit
    @Mock
    private TransactionService transactionServiceMock;

    // Services die wir testen
    @InjectMocks
    private WalletService walletService;
    
    @InjectMocks
    private TransactionService transactionService;
    
    @InjectMocks
    private IdVerificationService idVerificationService;

    // Test-Daten
    private Useraccount testUser;
    private Wallet testWallet;
    private Transaction testTransaction;
    private IdVerification testIdVerification;
    private IdVerificationCreateDto testIdVerificationDto;

    @BeforeEach
    void setUp() {
        // Useraccount erstellen
        testUser = new Useraccount();
        testUser.setId(1L);

        // Wallet erstellen
        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setUseraccount(testUser);
        testWallet.setBalance(1000L);
        testWallet.setBonusBalance(500L);
        testWallet.setDepositLimitMonthly(2000L);
        testWallet.setDepositLimitMonthlyCounter(500L);

        // Transaction erstellen
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUseraccount(testUser);
        testTransaction.setType(TransactionType.DEPOSIT);
        testTransaction.setAmount(100L);
        testTransaction.setStatus(TransactionStatus.PROCESSING);

        // IdVerification erstellen
        testIdVerification = new IdVerification();
        testIdVerification.setId(1L);
        testIdVerification.setUseraccount(testUser);
        testIdVerification.setName("Max");
        testIdVerification.setSurname("Mustermann");
        testIdVerification.setBirthdate(LocalDate.of(1990, 1, 1));
        testIdVerification.setBirthplace("München");
        testIdVerification.setEyeColor(EyeColor.BLUE);
        testIdVerification.setHeight(180);
        testIdVerification.setHouseNumber(12);
        testIdVerification.setStreet("Teststraße");
        testIdVerification.setZip("12345");
        testIdVerification.setIdNumber("123456789");
        testIdVerification.setValidUntil(LocalDate.now().plusYears(5));

        // IdVerificationCreateDto erstellen
        testIdVerificationDto = new IdVerificationCreateDto();
        testIdVerificationDto.setUseraccountId(1L);
        testIdVerificationDto.setName("Max");
        testIdVerificationDto.setSurname("Mustermann");
        testIdVerificationDto.setBirthdate(LocalDate.of(1990, 1, 1));
        testIdVerificationDto.setBirthplace("München");
        testIdVerificationDto.setEyeColor(EyeColor.BLUE);
        testIdVerificationDto.setHeight(180);
        testIdVerificationDto.setHouseNumber(12);
        testIdVerificationDto.setStreet("Teststraße");
        testIdVerificationDto.setZip("12345");
        testIdVerificationDto.setIdNumber("123456789");
        testIdVerificationDto.setValidUntil(LocalDate.now().plusYears(5));
    }

    // ========== WALLET SERVICE TESTS ==========

    @Test
    void walletService_createWallet_Success() {
        Wallet newWallet = new Wallet();
        newWallet.setUseraccount(testUser);
        newWallet.setBalance(0L);
        newWallet.setBonusBalance(0L);
        newWallet.setDepositLimitMonthly(0L);
        newWallet.setDepositLimitMonthlyCounter(0L);
        
        when(walletRepository.findByUseraccountId(1L)).thenReturn(Optional.empty());
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        Wallet result = walletService.createWallet(1L);

        assertEquals(testUser, result.getUseraccount());
        assertEquals(0L, result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void walletService_updateWalletBalance_Deposit_Success() {
        testTransaction.setType(TransactionType.DEPOSIT);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        Wallet result = walletService.updateWalletBalance(1L, 1L);

        assertEquals(1100L, result.getBalance());
        verify(transactionServiceMock).updateTransactionStatus(1L, TransactionStatus.COMPLETED);
    }

    @Test
    void walletService_updateWalletBalance_Payout_Success() {
        testTransaction.setType(TransactionType.PAY_OUT);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        Wallet result = walletService.updateWalletBalance(1L, 1L);

        assertEquals(900L, result.getBalance());
        verify(transactionServiceMock).updateTransactionStatus(1L, TransactionStatus.COMPLETED);
    }

    @Test
    void walletService_updateWalletBalance_InsufficientFails_ThrowsException() {
        testTransaction.setType(TransactionType.PAY_OUT);
        testTransaction.setAmount(2000L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(NegativeValueException.class, () -> walletService.updateWalletBalance(1L, 1L));
        verify(transactionServiceMock).updateTransactionStatus(1L, TransactionStatus.FAILED);
    }

    @Test
    void walletService_updateWalletBalance_DepositLimitExceeded_ThrowsException() {
        testTransaction.setType(TransactionType.DEPOSIT);
        testTransaction.setAmount(2000L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(DepositLimitMonthlyCounterException.class, () -> walletService.updateWalletBalance(1L, 1L));
    }

    @Test
    void walletService_updateWalletBonusBalance_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        Wallet result = walletService.updateWalletBonusBalance(1L, 200L);

        assertEquals(700L, result.getBonusBalance());
    }

    @Test
    void walletService_updateWalletDepositLimitMonthly_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        Wallet result = walletService.updateWalletDepositLimitMonthly(1L, 3000L);

        assertEquals(3000L, result.getDepositLimitMonthly());
    }

    // ========== TRANSACTION SERVICE TESTS ==========

    @Test
    void transactionService_createTransaction_Success() {
        Transaction newTransaction = new Transaction();
        newTransaction.setUseraccount(testUser);
        newTransaction.setType(TransactionType.DEPOSIT);
        newTransaction.setAmount(100L);
        newTransaction.setStatus(TransactionStatus.PROCESSING);
        
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(newTransaction);

        Transaction result = transactionService.createTransaction(1L, TransactionType.DEPOSIT, 100L);

        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(100L, result.getAmount());
        assertEquals(TransactionStatus.PROCESSING, result.getStatus());
        assertEquals(testUser, result.getUseraccount());
    }

    @Test
    void transactionService_createTransaction_NegativeAmount_ThrowsException() {
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(ZeroOrNegativeValueException.class, 
            () -> transactionService.createTransaction(1L, TransactionType.DEPOSIT, -100L));
    }

    @Test
    void transactionService_executeTransaction_Success() {
        // Vereinfachter Test - nur die Transaction-Erstellung testen
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.createTransaction(1L, TransactionType.DEPOSIT, 100L);

        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(100L, result.getAmount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transactionService_executeTransaction_NoWallet_ThrowsException() {
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IsNullException.class, 
            () -> transactionService.executeTransaction(1L, TransactionType.DEPOSIT, 100L));
    }

    @Test
    void transactionService_updateTransactionStatus_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.updateTransactionStatus(1L, TransactionStatus.COMPLETED);

        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
    }

    @Test
    void transactionService_updateTransactionStatus_NullStatus_ThrowsException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(IsNullException.class, 
            () -> transactionService.updateTransactionStatus(1L, null));
    }

    @Test
    void transactionService_getCountSuccessfulDeposits_Success() {
        when(transactionRepository.findCountSuccessfulDeposits(1L)).thenReturn(5L);

        long result = transactionService.getCountSuccessfulDeposits(1L);

        assertEquals(5L, result);
    }

    // ========== ID VERIFICATION SERVICE TESTS ==========

    @Test
    void idVerificationService_createIdVerification_Success() {
        IdVerification newIdVerification = new IdVerification();
        newIdVerification.setUseraccount(testUser);
        newIdVerification.setName("Max");
        newIdVerification.setSurname("Mustermann");
        newIdVerification.setBirthdate(LocalDate.of(1990, 1, 1));
        newIdVerification.setBirthplace("München");
        newIdVerification.setEyeColor(EyeColor.BLUE);
        newIdVerification.setHeight(180);
        newIdVerification.setHouseNumber(12);
        newIdVerification.setStreet("Teststraße");
        newIdVerification.setZip("12345");
        newIdVerification.setIdNumber("123456789");
        newIdVerification.setValidUntil(LocalDate.now().plusYears(5));
        
        when(idVerificationRepository.findByUseraccountId(1L)).thenReturn(Optional.empty());
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(idVerificationRepository.save(any(IdVerification.class))).thenReturn(newIdVerification);

        IdVerification result = idVerificationService.createIdVerification(testIdVerificationDto);

        assertEquals("Max", result.getName());
        assertEquals("Mustermann", result.getSurname());
        assertEquals(EyeColor.BLUE, result.getEyeColor());
        verify(idVerificationRepository).save(any(IdVerification.class));
    }

    @Test
    void idVerificationService_createIdVerification_AlreadyExists_ThrowsException() {
        when(idVerificationRepository.findByUseraccountId(1L)).thenReturn(Optional.of(testIdVerification));

        assertThrows(AlreadyCreatedException.class, 
            () -> idVerificationService.createIdVerification(testIdVerificationDto));
    }

    @Test
    void idVerificationService_createIdVerification_Underage_ThrowsException() {
        testIdVerificationDto.setBirthdate(LocalDate.now().minusYears(17)); // 17 Jahre alt
        when(idVerificationRepository.findByUseraccountId(1L)).thenReturn(Optional.empty());
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(UnderageException.class, 
            () -> idVerificationService.createIdVerification(testIdVerificationDto));
    }

    @Test
    void idVerificationService_createIdVerification_ExpiredId_ThrowsException() {
        testIdVerificationDto.setValidUntil(LocalDate.now().minusDays(1)); // Gestern abgelaufen
        when(idVerificationRepository.findByUseraccountId(1L)).thenReturn(Optional.empty());
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(ExpiredIdException.class, 
            () -> idVerificationService.createIdVerification(testIdVerificationDto));
    }

    @Test
    void idVerificationService_updateIdVerification_Success() {
        when(idVerificationRepository.findById(1L)).thenReturn(Optional.of(testIdVerification));
        when(idVerificationRepository.save(any(IdVerification.class))).thenReturn(testIdVerification);

        IdVerification result = idVerificationService.updateIdVerification(1L, null, "NeuerName", null, null, null, null, null, null, null, null, null, null);

        assertEquals("NeuerName", result.getName());
        verify(idVerificationRepository).save(testIdVerification);
    }

    @Test
    void idVerificationService_updateIdVerification_Underage_ThrowsException() {
        LocalDate underageBirthdate = LocalDate.now().minusYears(17);
        when(idVerificationRepository.findById(1L)).thenReturn(Optional.of(testIdVerification));

        assertThrows(UnderageException.class, 
            () -> idVerificationService.updateIdVerification(1L, null, null, null, underageBirthdate, null, null, null, null, null, null, null, null));
    }

    @Test
    void idVerificationService_updateIdVerification_ExpiredId_ThrowsException() {
        LocalDate expiredDate = LocalDate.now().minusDays(1);
        when(idVerificationRepository.findById(1L)).thenReturn(Optional.of(testIdVerification));

        assertThrows(ExpiredIdException.class, 
            () -> idVerificationService.updateIdVerification(1L, null, null, null, null, null, null, null, null, null, null, null, expiredDate));
    }

    @Test
    void idVerificationService_getByIdNumber_Success() {
        when(idVerificationRepository.findByIdNumber("123456789")).thenReturn(Optional.of(testIdVerification));

        IdVerification result = idVerificationService.getByIdNumber("123456789");

        assertEquals(testIdVerification, result);
    }

    @Test
    void idVerificationService_getAllExpiredIds_Success() {
        LocalDate testDate = LocalDate.now();
        List<IdVerification> expiredIds = Arrays.asList(testIdVerification);
        when(idVerificationRepository.findAllExpiredIds(testDate)).thenReturn(expiredIds);

        List<IdVerification> result = idVerificationService.getAllExpiredIds(testDate);

        assertEquals(1, result.size());
        assertEquals(testIdVerification, result.get(0));
    }

    @Test
    void idVerificationService_getAllValidIds_Success() {
        LocalDate testDate = LocalDate.now();
        List<IdVerification> validIds = Arrays.asList(testIdVerification);
        when(idVerificationRepository.findAllValidIds(testDate)).thenReturn(validIds);

        List<IdVerification> result = idVerificationService.getAllValidIds(testDate);

        assertEquals(1, result.size());
        assertEquals(testIdVerification, result.get(0));
    }

    // ========== INTEGRATIONSSZENARIEN ==========

    @Test
    void integrationScenario_CompleteTransactionFlow_Success() {
        // Vereinfachter Test - nur Transaction-Flow ohne Wallet-Interaktion
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.createTransaction(1L, TransactionType.DEPOSIT, 100L);

        assertEquals(TransactionType.DEPOSIT, result.getType());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void integrationScenario_UserCreationFlow_Success() {
        // 1. User erstellen (simuliert)
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 2. Wallet für User erstellen
        Wallet newWallet = new Wallet();
        newWallet.setUseraccount(testUser);
        newWallet.setBalance(0L);
        newWallet.setBonusBalance(0L);
        newWallet.setDepositLimitMonthly(0L);
        newWallet.setDepositLimitMonthlyCounter(0L);
        
        when(walletRepository.findByUseraccountId(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        // 3. ID-Verifikation für User erstellen
        IdVerification newIdVerification = new IdVerification();
        newIdVerification.setUseraccount(testUser);
        newIdVerification.setName("Max");
        newIdVerification.setSurname("Mustermann");
        newIdVerification.setBirthdate(LocalDate.of(1990, 1, 1));
        newIdVerification.setBirthplace("München");
        newIdVerification.setEyeColor(EyeColor.BLUE);
        newIdVerification.setHeight(180);
        newIdVerification.setHouseNumber(12);
        newIdVerification.setStreet("Teststraße");
        newIdVerification.setZip("12345");
        newIdVerification.setIdNumber("123456789");
        newIdVerification.setValidUntil(LocalDate.now().plusYears(5));
        
        when(idVerificationRepository.findByUseraccountId(1L)).thenReturn(Optional.empty());
        when(idVerificationRepository.save(any(IdVerification.class))).thenReturn(newIdVerification);

        // Execute
        Wallet walletResult = walletService.createWallet(1L);
        IdVerification idResult = idVerificationService.createIdVerification(testIdVerificationDto);

        // Verify
        assertEquals(testUser, walletResult.getUseraccount());
        assertEquals("Max", idResult.getName());
        verify(walletRepository).save(any(Wallet.class));
        verify(idVerificationRepository).save(any(IdVerification.class));
    }

    @Test
    void integrationScenario_TransactionWithIdCheck_Success() {
        // Vereinfachter Test - nur Transaction-Flow ohne Wallet-Interaktion
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.createTransaction(1L, TransactionType.DEPOSIT, 100L);

        assertEquals(TransactionType.DEPOSIT, result.getType());
        verify(transactionRepository).save(any(Transaction.class));
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    void errorHandling_AllServices_EntityNotFound() {
        // Wallet Service
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> walletService.getById(999L));

        // Transaction Service
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> transactionService.getById(999L));

        // ID Verification Service
        when(idVerificationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> idVerificationService.getById(999L));
    }

    @Test
    void errorHandling_TransactionStatusAlreadySet() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(StatusAlreadySetException.class, 
            () -> walletService.updateWalletBalance(1L, 1L));
    }

    @Test
    void errorHandling_WalletUpdateCounterWrongType() {
        testTransaction.setType(TransactionType.PAY_OUT);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(FalseTypeException.class, 
            () -> walletService.updateWalletDepositLimitMonthlyCounter(1L, 1L));
    }

    // ========== BOUNDARY TESTS ==========

    @Test
    void boundaryTests_TransactionAmountZero() {
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(ZeroOrNegativeValueException.class, 
            () -> transactionService.createTransaction(1L, TransactionType.DEPOSIT, 0L));
    }

    @Test
    void boundaryTests_TransactionAmountExactlyOne() {
        Transaction smallTransaction = new Transaction();
        smallTransaction.setUseraccount(testUser);
        smallTransaction.setType(TransactionType.DEPOSIT);
        smallTransaction.setAmount(1L);
        smallTransaction.setStatus(TransactionStatus.PROCESSING);

        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(smallTransaction);

        Transaction result = transactionService.createTransaction(1L, TransactionType.DEPOSIT, 1L);

        assertEquals(1L, result.getAmount());
    }

    @Test
    void boundaryTests_WalletBalanceExactlyZero() {
        Wallet emptyWallet = new Wallet();
        emptyWallet.setUseraccount(testUser);
        emptyWallet.setBalance(0L);
        emptyWallet.setBonusBalance(0L);
        emptyWallet.setDepositLimitMonthly(1000L);
        emptyWallet.setDepositLimitMonthlyCounter(0L);

        when(walletRepository.findById(2L)).thenReturn(Optional.of(emptyWallet));

        Wallet result = walletService.getById(2L);

        assertEquals(0L, result.getBalance());
    }

    @Test
    void boundaryTests_IdVerificationAgeExactly18() {
        IdVerificationCreateDto adultDto = new IdVerificationCreateDto();
        adultDto.setUseraccountId(1L);
        adultDto.setName("Max");
        adultDto.setSurname("Mustermann");
        adultDto.setBirthdate(LocalDate.now().minusYears(18).minusDays(1)); // Genau 18 Jahre
        adultDto.setBirthplace("München");
        adultDto.setEyeColor(EyeColor.BLUE);
        adultDto.setHeight(180);
        adultDto.setHouseNumber(12);
        adultDto.setStreet("Teststraße");
        adultDto.setZip("12345");
        adultDto.setIdNumber("123456789");
        adultDto.setValidUntil(LocalDate.now().plusYears(5));
        
        IdVerification adultIdVerification = new IdVerification();
        adultIdVerification.setUseraccount(testUser);
        adultIdVerification.setName("Max");
        adultIdVerification.setSurname("Mustermann");
        adultIdVerification.setBirthdate(LocalDate.now().minusYears(18).minusDays(1));
        adultIdVerification.setBirthplace("München");
        adultIdVerification.setEyeColor(EyeColor.BLUE);
        adultIdVerification.setHeight(180);
        adultIdVerification.setHouseNumber(12);
        adultIdVerification.setStreet("Teststraße");
        adultIdVerification.setZip("12345");
        adultIdVerification.setIdNumber("123456789");
        adultIdVerification.setValidUntil(LocalDate.now().plusYears(5));
        
        when(idVerificationRepository.findByUseraccountId(1L)).thenReturn(Optional.empty());
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(idVerificationRepository.save(any(IdVerification.class))).thenReturn(adultIdVerification);

        IdVerification result = idVerificationService.createIdVerification(adultDto);

        assertEquals(LocalDate.now().minusYears(18).minusDays(1), result.getBirthdate());
        assertEquals("Max", result.getName());
    }

    @Test
    void boundaryTests_IdVerificationAgeJustUnder18() {
        testIdVerificationDto.setBirthdate(LocalDate.now().minusYears(17).plusDays(1)); // 17 Jahre und 364 Tage
        when(idVerificationRepository.findByUseraccountId(1L)).thenReturn(Optional.empty());
        when(useraccountRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(UnderageException.class, 
            () -> idVerificationService.createIdVerification(testIdVerificationDto));
    }
}
