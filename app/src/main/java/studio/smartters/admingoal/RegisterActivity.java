package studio.smartters.admingoal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private LinearLayout work_block;
    private FirebaseAuth mAuth;
    private EditText mName,mEmail;
    private Spinner sType,sWork;
    private String uType,uWork;
    private final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        work_block = findViewById(R.id.works_block);
        mName = findViewById(R.id.user_name);
        mEmail = findViewById(R.id.user_email);
        sType = findViewById(R.id.user_type);
        sWork = findViewById(R.id.user_work);
        mAuth = FirebaseAuth.getInstance();

        List<String> spinnerArray = Arrays.asList(this.getResources().getStringArray(R.array.sa));
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.spinner_item,spinnerArray);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        List<String> spinnerArray1 = Arrays.asList(this.getResources().getStringArray(R.array.works));
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item,spinnerArray1);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sType.setAdapter(adapter1);
        sWork.setAdapter(adapter2);
        sType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==1)
                {
                    uType = "a";
                    work_block.setVisibility(View.VISIBLE);
                }else{
                    uType = "s";
                    work_block.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sWork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                uWork = sWork.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void exitApp(View view) {
        //finishAffinity();
        finish();
    }
    public void registerNow(View view) {
        final String email,password;
        email = mEmail.getText().toString();
        if(email.equals("")||mName.getText().toString().equals(""))
        {
            Toast.makeText(this, "Fields Can't be empty.", Toast.LENGTH_SHORT).show();
        }else {
            password = randomAlphaNumeric(8);
            final ProgressDialog pd1 = new ProgressDialog(RegisterActivity.this,R.style.AppCompatAlertDialogStyle);
            pd1.setTitle("Please Wait");
            pd1.setMessage("Creating Your Account");
            pd1.setCanceledOnTouchOutside(false);
            pd1.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success
                        if (uType.equals("s"))
                            uWork = "none";
                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(uType + "/" + uWork)
                                .build();
                        user.updateProfile(profileUpdates);
                        Map<String , Object> map = new HashMap<>();
                        map.put("name",mName.getText().toString());
                        map.put("company",uWork);
                        map.put("dp","none");
                        FirebaseDatabase.getInstance().getReference().child("users").child(task.getResult().getUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();
                                    pd1.dismiss();
                                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                new AlertDialog.Builder(RegisterActivity.this,R.style.AlertDialogTheme)
                                                        .setTitle("Registration Success")
                                                        .setMessage("Ask the User to set his/her password by clicking on the Password Reset Email sent to this perticular user.")
                                                        .setPositiveButton("Yes, Sure", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                finish();
                                                            }
                                                        }).show();
                                            }
                                            else{
                                                Toast.makeText(RegisterActivity.this, "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                pd1.dismiss();
                                            }
                                        }
                                    });
                                }else{
                                    Toast.makeText(RegisterActivity.this, "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    pd1.dismiss();
                                }
                            }
                        });
                    }else{
                        pd1.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    public String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
