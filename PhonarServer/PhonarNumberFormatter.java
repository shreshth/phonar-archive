import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhonarNumberFormatter {
    public static void main (String[] args) {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            PhoneNumber phoneNumber = null;
            try {
                phoneNumber = phoneUtil.parse(args[0], args[1]);
            } catch (NumberParseException e) {
                System.err.print(e.toString());
            }
            System.out.print(phoneUtil.format(phoneNumber, PhoneNumberFormat.E164));
    }
}