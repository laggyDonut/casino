package de.edvschuleplattling.irgendwieanders.service;

import de.edvschuleplattling.irgendwieanders.Exceptions.*;
import de.edvschuleplattling.irgendwieanders.config.GlobalConstants;
import de.edvschuleplattling.irgendwieanders.model.transaction.Transaction;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionStatus;
import de.edvschuleplattling.irgendwieanders.model.transaction.TransactionType;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import de.edvschuleplattling.irgendwieanders.repository.TransactionRepository;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
    @Transactional
    public class WalletService {

        private final WalletRepository walletRepository;
        private final UseraccountRepository useraccountRepository;
        private final TransactionRepository transactionRepository;
        private final TransactionService transactionService;

        //Konstruktor, damit lazy geladen werden kann
    @Lazy
    public WalletService(
            WalletRepository walletRepository,
            UseraccountRepository useraccountRepository,
            TransactionRepository transactionRepository,
            @org.springframework.context.annotation.Lazy TransactionService transactionService // HIER DAS LAZY
    ) {
        this.walletRepository = walletRepository;
        this.useraccountRepository = useraccountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public List<Wallet> getAll(){
        return walletRepository.findAll();
    }

    @Transactional
    public Wallet getById(long id){
        return walletRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Wallet getByUseraccountId(long useraccountId){
        return walletRepository.findByUseraccountId(useraccountId).orElseThrow();
    }

        @Transactional
        public Wallet createWallet (long useraccountId)
        {

            //Gibt es schon ein Wallet?
            if (walletRepository.findByUseraccountId(useraccountId).isPresent()) {
                throw new NoSuchElementException("Es gibt bereits ein Wallet des User mit der ID " + useraccountId + ".");
            }

            //Gibt es User?
            Useraccount u = useraccountRepository.findById(useraccountId).orElseThrow();

            //Objekt anlegen
            Wallet w = new Wallet(u, 0, 0, 0, 0);
            walletRepository.save(w);

            return w;
        }

        //HIER NOCH EINE JAVADOC BESCHREIBUNG FÜR METHODE UPDATEWALLETBALANCE, DA KOMPLEX!
        @Transactional
        public Wallet updateWalletBalance(long id, long transactionId) {

            //Gibt es Wallet?
            Wallet w = walletRepository.findById(id).orElseThrow();

            //Gibt es Transaction?
            Transaction t = transactionRepository.findById(transactionId).orElseThrow();

            //Zur Sicherheit: Wurde eine Transaction davor schon auf COMPLETED, FAILED ODER LOCKED gesetzt?
            if (t.getStatus() != TransactionStatus.PROCESSING) {
                throw new StatusAlreadySetException("Status der Transaktion wurde schon auf " + t.getStatus() + " gesetzt.");
            }

            //TODO: Hier können später noch weitere Prüfungen ergänzt werden, welche eine Exception auslösen
            //TODO: und status auf LOCKED oder FAILED setzen

            //Umrechnung von balance in die virtuelle Währung
            long points = GlobalConstants.cashToPoints(t.getCashAmount());

            switch(t.getType()){
                case DEPOSIT:

                    //Falls depositLimitMonthlyCounter überschritten wurde, wird hier eine Exception geworfen
                    //Bei depositLimitMonthly = 0 --> es wird keine Exceptino geworfen, da 0 kein Limit bedeutet
                    if (w.getDepositLimitMonthly() > 0 &&
                            w.getDepositLimitMonthlyCounter() + t.getCashAmount() > w.getDepositLimitMonthly()) {
                        throw new DepositLimitMonthlyCounterException("Das monatliche Einzahlungslimit wurde überschritten.");
                    }

                    w.setBalance(w.getBalance() + points);

                    //Hier könnten Methoden aufgerufen werden, welche das Geld vom Bankkonto des Users abbuchen
                    //In unserem Fall wird das jedoch nur simuliert

                    //Das Attribut status der Transaction wird nun auf COMPLETED gesetzt
                    transactionService.updateTransactionStatus(t.getId(), TransactionStatus.COMPLETED);

                    //Aktualisierung von depositLimitMonthlyCounter
                    updateWalletDepositLimitMonthlyCounter(w.getId(), t.getId());
                    break;
                case PAY_OUT:

                    //Zur Sicherheit: balance kann nicht negativ werden
                    if (w.getBalance() < points) {

                        transactionService.updateTransactionStatus(t.getId(), TransactionStatus.FAILED);
                        throw new NegativeValueException("Das Konto-Guthaben ist für diese Auszahlung nicht ausreichend.");

                    }

                    w.setBalance(w.getBalance() - points);

                    //Hier könnten Methoden aufgerufen werden, welche das Geld auf das Bankkonto des Users buchen
                    //In unserem Fall wird das jedoch nur simuliert

                    //Das Attribut status der Transaction wird nun auf COMPLETED gesetzt
                    transactionService.updateTransactionStatus(t.getId(), TransactionStatus.COMPLETED);
                    break;
            }

            walletRepository.save(w);

            return w;
        }

        @Transactional
        public Wallet updateWalletBonusBalance(long id, long bonusPoints) {

            //Gibt es Wallet?
            Wallet w = walletRepository.findById(id).orElseThrow();

            //Ist Bonus > 0?
            if (bonusPoints <= 0) {
                throw new ZeroOrNegativeValueException("Der Bonusbetrag beträgt 0 oder hat einen negativen Wert.");
            }

            w.setBonusBalance(w.getBonusBalance() + bonusPoints);

            walletRepository.save(w);

            return w;
        }

        @Transactional
        public Wallet updateWalletDepositLimitMonthly(long id, long depositLimitMonthly) {

            //Gibt es Wallet?
            Wallet w = walletRepository.findById(id).orElseThrow();

            //Ist depositLimitMonthly > 0?
            if (depositLimitMonthly <= 0) {
                throw new ZeroOrNegativeValueException("Das Einzahlungslimit beträgt 0 oder hat einen negativen Wert.");
            }

            w.setDepositLimitMonthly(depositLimitMonthly);

            walletRepository.save(w);

            return w;
        }

        @Transactional
        public Wallet updateWalletDepositLimitMonthlyCounter(long id, long transactionID) {

            //Gibt es Wallet?
            Wallet w = walletRepository.findById(id).orElseThrow();

            //Gibt es Transaction?
            Transaction t = transactionRepository.findById(transactionID).orElseThrow();

            //Nur DEPOSIT-Transactions dürfen verwendet werden
            if (t.getType() == TransactionType.DEPOSIT) {

                w.setDepositLimitMonthlyCounter(w.getDepositLimitMonthlyCounter() + t.getCashAmount());

                walletRepository.save(w);

                return w;
            } else {
                throw new FalseTypeException("Um den Einzahlungslimit-Counter zu erhöhen, muss eine " +
                        "Transaktion mit Type DEPOSIT übergeben werden.");
            }
        }

        // Die Methode soll nur automatisch vom System aufgerufen werden können --> package-private
        @Transactional
        @Scheduled(cron = "0 0 0 1 * *")
        void counterUpdater(){
            walletRepository.resetAllDepositLimitMonthlyCounter();
        }

    @Transactional
    public void deleteWallet(long id) {

        //Gibt es die Id?
        walletRepository.findById(id).orElseThrow();

        //Löschen
        walletRepository.deleteById(id);
    }

























}
