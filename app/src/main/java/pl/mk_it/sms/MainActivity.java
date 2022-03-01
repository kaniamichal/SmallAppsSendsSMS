package pl.mk_it.sms;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText phone, hourVisit, value;
    TimePickerDialog timePicker;
    final int SEND_SMS_PERMISSION_REQUEST = 1;
    final int PICK_CONTACT_REQUEST = 1;
    public static final String SENT_SMS_ACTION_NAME = "SMS_SENT";
    public static final String DELIVERED_SMS_ACTION_NAME = "SMS_DELIVERED";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phone = findViewById(R.id.editTextNumerPhone);
        value = findViewById(R.id.editValue);
        hourVisit = findViewById(R.id.editTextHour);
        hourVisit.setInputType(InputType.TYPE_NULL);
        hourVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                timePicker = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @SuppressLint({"SetTextI18n", "DefaultLocale"})
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                hourVisit.setText(String.format("%02d:%02d", sHour,sMinute));
                            }
                        }, hour, minutes, true);
                timePicker.show();
            }
        });
        Button btnConfirm = findViewById(R.id.btnConfirm);
        Button btnContact = findViewById(R.id.btnContact);
        Button btnDelete = findViewById(R.id.btnDeleteEvents);
        Button btnFirst = findViewById(R.id.btnFirst);


        btnConfirm.setEnabled(false);
        btnContact.setEnabled(false);
        btnDelete.setEnabled(false);
        btnFirst.setEnabled(false);

        if (checkPermission(Manifest.permission.SEND_SMS)) {
            btnConfirm.setEnabled(true);
            btnContact.setEnabled(true);
            btnDelete.setEnabled(true);
            btnFirst.setEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST);
        }
    }

    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    public void smsSend(View view) {

        String phoneNumber = phone.getText().toString();
        String hourEvents = hourVisit.getText().toString();
        String valueCost = value.getText().toString();

        if (("".equals(phone.getText().toString().trim()) || "".equals(hourVisit.getText().toString().trim())
                || "".equals(value.getText().toString().trim()))) {
            Toast.makeText(this, "Dagu nie wpisałaś wszystkich danych :)", Toast.LENGTH_SHORT).show();
            return;
        }

        String msgFirst = "Witam :) " + '\n' +
                "Przypominam o jutrzejszej wizycie fizjoterapeutycznej na godz. " + hourEvents + '\n' +
                "Informuję, że miejsce docelowe to budynek, który w centralnym punkcie ma Aptekę " +
                "(biały napis na zielonym tle). Wejście do nas jest od lewej strony budynku po schodach. " +
                "Będzie tam widniała tabliczka Poradnia dla kobiet MAG-MED oraz PelviCare :)" + '\n' +
                "Adres to ul. Człuchowska 12a/19 w Przechlewie. Czas trwania wizyty ok. 60 min" + '\n'+
                "Proszę zabrać ze sobą wygodne ubranie." + '\n' +
                "Koszt wizyty to " + valueCost + " zł." +'\n' +
                "Proszę o potwierdzenie przybycia." + '\n' +
                "Pozdrawiam" + '\n' +
                "Dagmara Kołodziejska";

//        if (phoneNumber.length() == 0 || hourEvents.length() == 0 || valueCost.length() == 0) {
//        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(hourEvents) || TextUtils.isEmpty(valueCost)) {
//            Toast.makeText(this, "Uzupełnij pola", Toast.LENGTH_SHORT).show();
//        }
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT_SMS_ACTION_NAME), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED_SMS_ACTION_NAME), 0);

        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(msgFirst);

            ArrayList<PendingIntent> sendList = new ArrayList<>();
            sendList.add(sentPI);

            ArrayList<PendingIntent> deliverList = new ArrayList<>();
            deliverList.add(deliveredPI);

            sms.sendMultipartTextMessage(phoneNumber, null, parts, sendList, deliverList);
            Toast.makeText(this, "Dagu Twoja wiadomość polecaiała do adresata :)", Toast.LENGTH_SHORT).show();
            phone.getText().clear();
            hourVisit.getText().clear();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Napisz do Szwagra ;) Coś poszło nie tak - wiadomość nie wysłana", Toast.LENGTH_SHORT).show();
        }
    }



    @SuppressLint("IntentReset")
    public void phoneNumber(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(contactPickerIntent, PICK_CONTACT_REQUEST);
    }


    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {

        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == PICK_CONTACT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = getContentResolver().query(contactData, null,
                        null, null, null);
                if (c.moveToFirst()) {
                    int numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String num = "";
                    num = c.getString(numberIndex);
                    phone.setText(num);
                } else {
                    Log.e("Failed", "Not able to pick contact");
                }
            }
        }
    }


    public void smsDeleted(View view) {
        String phoneNumber2 = phone.getText().toString();
        String hourEvents2 = hourVisit.getText().toString();
        String msgDelete = "Z przykrością odwołuję umówioną wizytę na jutro na godzinę " + hourEvents2 + '\n' +
                "O wyznaczeniu nowego terminu poinformuję niezwłocznie." + '\n' +
                "Pozdrawiam Dagmara Kołodziejska.";

        if (("".equals(phone.getText().toString().trim()) || "".equals(hourVisit.getText().toString().trim()))) {
            Toast.makeText(this, "Dagu nie wpisałaś wszystkich danych :)", Toast.LENGTH_SHORT).show();
            return;
        }
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT_SMS_ACTION_NAME), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED_SMS_ACTION_NAME), 0);

        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(msgDelete);

            ArrayList<PendingIntent> sendList = new ArrayList<>();
            sendList.add(sentPI);

            ArrayList<PendingIntent> deliverList = new ArrayList<>();
            deliverList.add(deliveredPI);

            sms.sendMultipartTextMessage(phoneNumber2, null, parts, sendList, deliverList);
            Toast.makeText(this, "Message send!", Toast.LENGTH_SHORT).show();
            phone.getText().clear();
            hourVisit.getText().clear();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Napisz do Szwagra ;) Coś poszło nie tak - wiadomość nie wysłana",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void smsConfirm(View view) {
        String phoneNumber = phone.getText().toString();
        String hourEvents = hourVisit.getText().toString();
        String msgConfirm = "Witam :) " + '\n' +
                "Przypominam o jutrzejszej wizycie fizjoterapeutycznej o godz. " + hourEvents + '\n' +
                "Prypominam o wygodnym ubraniu" + '\n' +
                "Proszę o potwierdzenie przybycia." + '\n' +
                "Pozdrawiam Dagmara Kołodziejska";

        if (("".equals(phone.getText().toString().trim()) || "".equals(hourVisit.getText().toString().trim()))) {
            Toast.makeText(this, "Dagu nie wpisałaś wszystkich danych :)", Toast.LENGTH_SHORT).show();
            return;
        }
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT_SMS_ACTION_NAME), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED_SMS_ACTION_NAME), 0);

        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(msgConfirm);

            ArrayList<PendingIntent> sendList = new ArrayList<>();
            sendList.add(sentPI);

            ArrayList<PendingIntent> deliverList = new ArrayList<>();
            deliverList.add(deliveredPI);

            sms.sendMultipartTextMessage(phoneNumber, null, parts, sendList, deliverList);
            Toast.makeText(this, "Message send!", Toast.LENGTH_SHORT).show();
            phone.getText().clear();
            hourVisit.getText().clear();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Napisz do Szwagra ;) Coś poszło nie tak - wiadomość nie wysłana", Toast.LENGTH_SHORT).show();
        }

    }
}
