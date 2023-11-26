import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PrankGenerator {
    private ConfigurationManager configManager;
    private SMTPClient smtpClient;
    private int numberOfGroups;

    public PrankGenerator(ConfigurationManager configManager, int numberOfGroups) {
        this.configManager = configManager;
        this.numberOfGroups = numberOfGroups;

        SMTPConfig smtpConfig = configManager.readSMTPConfig();
        this.smtpClient = new SMTPClient(smtpConfig.getHost(), smtpConfig.getPort());
    }

    public void generateAndSendPranks() throws IOException {
        List<String> victimEmails = configManager.readVictimEmailAddresses();
        List<String> emailMessages = configManager.readEmailMessages();

        // Shuffle the list for randomness
        Collections.shuffle(victimEmails);

        int groupSize = Math.max(2, victimEmails.size() / numberOfGroups);
        Random random = new Random();

        for (int i = 0; i < numberOfGroups; i++) {
            EmailGroup group = new EmailGroup();
            // Assign sender
            group.setSender(victimEmails.remove(0));

            // Assign recipients
            for (int j = 0; j < groupSize - 1 && !victimEmails.isEmpty(); j++) {
                group.addRecipient(victimEmails.remove(0));
            }

            // Assign a random email message to the group
            String message = emailMessages.get(random.nextInt(emailMessages.size()));
            group.setEmailMessage(message);

            sendEmail(group);
        }
    }

    private void sendEmail(EmailGroup group) throws IOException {
        smtpClient.connect();

        smtpClient.sendEHLO();
        smtpClient.sendMAILFROM(group.getSender());

        for (String recipient : group.getRecipients()) {
            smtpClient.sendRCPTTO(recipient);
        }

        smtpClient.sendData("Subject", group.getEmailMessage());
        smtpClient.sendQUIT();

        smtpClient.close();
    }
}