/// (ABDURAKHMON YUNUSOV)

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BankTransactionsReader {
    public static void main (String[] args) throws IOException {
        String fIn  = "C:\\Users\\ThinkPad\\IdeaProjects\\GUI_SecondProject\\src\\com\\company\\Bank.dat";
        String fErr = "C:\\Users\\ThinkPad\\IdeaProjects\\GUI_SecondProject\\src\\com\\company\\Bank.err";
         Map<String,Account> accs = readData(fIn,fErr);


        for (Map.Entry<String,Account> e : accs.entrySet())
            System.out.print(e.getValue());

        try {
            String errLog = new String(
                    Files.readAllBytes(Paths.get(fErr)), UTF_8);
            System.out.println("\nContent of " +
                    "\"Bank.err\" follows:\n");
            System.out.println(errLog);
        } catch(IOException e) {
            System.out.println("Problems with error log");
            return;
        }
    }
    public static Map<String, Account> readData(String fIn, String fErr) throws IOException {
        Map<String,Account> result = new HashMap<>() {};

        BufferedReader br = new BufferedReader(new FileReader(fIn));
        String line;
        File file = new File(fErr);
        FileWriter fw = new FileWriter(file);
        PrintWriter write = new PrintWriter(fw);

        while((line = br.readLine()) != null){ // reading line from file
            String[] middle = line.split(" ");
             if(middle.length == 4){
                 Person person = new Person(middle[1], middle[2]);
                 String id = middle[0];
                 if(result.containsKey(id)){
                     write.println("Line : " + line);
                     write.println("Error: Account already exists");
                 }else {
                     Account account = new Account(id, person, Integer.parseInt(middle[3]), new Transaction(id, Transaction.INIT_DEPOS, Integer.parseInt(middle[3])));

                     result.put(id, account);
                 }
             }else if(middle.length == 3){

                 String fromPerson = middle[0];
                 String toPerson = middle[1];
                 int amount = Integer.parseInt(middle[2]);

                 if(result.containsKey(fromPerson) && result.containsKey(toPerson)){ // checking these persons for Existence
                    Account fromAcc = result.get(fromPerson);
                    Account toAcc = result.get(toPerson);
                    if(fromAcc.balance >= amount){ // enough money to transfer

                        fromAcc.balance -= amount;
                        fromAcc.transactions.add(new Transaction(fromPerson, toPerson, amount, Transaction.TRANS_FROM));

                        toAcc.balance += amount;
                        toAcc.transactions.add(new Transaction(fromPerson, toPerson, amount, Transaction.TRANS_TO));

                        result.replace(fromPerson, fromAcc);
                        result.replace(toPerson, toAcc);
                    }else{ // luck of money to transfer
                        write.println("Line : " + line);
                        write.println("Error: Insufficient funds");
                    }

                 }else{ // there are no such persons
                     System.out.println("There are no such persons");
                 }



             }else if(middle.length == 2){
                 String idPerson = middle[0];
                 int amount = Integer.parseInt(middle[1]);

                 if(result.containsKey(idPerson)){ // if there is exist such person with id
                     Account tempAccount = result.get(idPerson);
                     if(amount < 0) { // Withdrawing
                         if(tempAccount.balance >= amount){
                             tempAccount.balance += amount;
                             tempAccount.transactions.add(new Transaction(idPerson, Transaction.WITHDRAWAL, amount));
                             result.replace(idPerson, tempAccount);
                         }else{
                             System.out.println("Not enough money on account");
                             // нужно добавить в ошибки с содер не хватает денег
                         }
                     }else{ // Deposit - adding money to account
                         tempAccount.balance += amount;
                         tempAccount.transactions.add(new Transaction(idPerson, Transaction.DEPOSIT, amount));
                         result.replace(idPerson, tempAccount);

                     }
                 } else{
                     System.out.println("No such person to add or withdraw money");
                     // No such person to add or withdraw money
                 }
             }else{
                 System.out.println("Invalid input");
             }
        }
        write.close();
        br.close();
        return result;
    }
}

class Transaction {
    public static final int INIT_DEPOS = 0;
    public static final int DEPOSIT = 1;
    public static final int WITHDRAWAL = 2;
    public static final int TRANS_FROM = 3;
    public static final int TRANS_TO = 4;
    private static final String[] opTypes =
            {
                    "Init depos ", "Deposit ", "Withdrawal ",
                    "Trans. from", "Trans. to "
            };
    public long timestamp;
    public String transDescription;

    public Transaction(String fromAcc, String toAcc, int amount, int tranType) {
        String message = null;
        if(tranType == 3) message = "-" + amount + ": " + opTypes[tranType] + " this account to " + toAcc;
        if(tranType == 4) message = amount + ": " + opTypes[tranType] + " this account from " + fromAcc;
        this.timestamp = System.currentTimeMillis();
        this.transDescription = message;
    }
    public Transaction(String toAcc, int tranType, int amount) {
        this.timestamp = System.currentTimeMillis();
        this.transDescription = amount + ": " + opTypes[tranType];
    }


    @Override
    public String toString() {
        return transDescription;
    }
}
class Account {
    private String id;
    private Person owner;
    public int balance;
    public List<Transaction> transactions = new ArrayList<>();

    public Account(String id, Person owner, int balance, Transaction tr) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
        this.transactions.add(tr);
    }

    @Override
    public String toString() {
        System.out.println("*** Acc " + id + "(" + owner + "). "
                + "Balance: " + balance + ". Transactions:");
        transactions.forEach((n) -> System.out.println("    " + n));
        return "";
    }
}
final class Person{
    private final String firstName;
    private final String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}


