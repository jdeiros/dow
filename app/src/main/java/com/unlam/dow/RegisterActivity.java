package com.unlam.dow;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import dmax.dialog.SpotsDialog;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unlam.dow.includes.MyToolbar;
import com.unlam.dow.models.User;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences mPref;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    TextInputEditText mTextInputName;
    TextInputEditText mTextInputLastName;
    TextInputEditText mTextInputDNI;
    TextInputEditText mTextInputEmailRegister;
    TextInputEditText mTextInputPasswordRegister;
    TextInputEditText mTextInputCommission;
    Button mButtonRegister;


    AlertDialog mDialog;
    public IntentFilter filter;
    //private ReceiverOperation receiver = new ReceiverOperation();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento").build();

        MyToolbar.show(this, "Registrar usuario", true);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);


        mTextInputName = findViewById(R.id.textInputName);
        mTextInputLastName = findViewById(R.id.textInputLastName);
        mTextInputDNI = findViewById(R.id.textInputDNI);
        mTextInputEmailRegister = findViewById(R.id.textInputEmailRegister);
        mTextInputPasswordRegister = findViewById(R.id.textInputPasswordRegister);
        mTextInputCommission = findViewById(R.id.textInputCommission);
        mButtonRegister = findViewById(R.id.btnRegister);
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //userRegister();
                registerUser();
            }
        });

        //cofigureBroadcastReceiver();
    }

    private void registerUser() {
        final String env = getString(R.string.environment);
        final String name = mTextInputName.getText().toString();
        final String lastname = mTextInputLastName.getText().toString();
        final Long dni = Long.parseLong(mTextInputDNI.getText().toString());
        final String email = mTextInputEmailRegister.getText().toString();
        final String password = mTextInputPasswordRegister.getText().toString();
        final Long commission = Long.parseLong(mTextInputCommission.getText().toString());
        
        if(!name.isEmpty() && !lastname.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 8){
                mDialog.show();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mDialog.hide();
                        if(task.isSuccessful()){
                            String id = mAuth.getCurrentUser().getUid();
                            saveUser(id, name, lastname, email);
                            
                        }else{
                            Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUser(String id, String name, String lastname, String email) {
        String selectedUser = mPref.getString("user", "");

        User user = new User();
        user.setEmail(email);
        user.setLastname(lastname);
        user.setName(name);

        if(selectedUser.equals("walker")){
            mDatabase.child("Users").child("Walkers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(RegisterActivity.this, "Falló el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if(selectedUser.equals("owner")){
            mDatabase.child("Users").child("Owners").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(RegisterActivity.this, "Falló el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }




  /*  private void userRegister() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("env", getString(R.string.environment));
            obj.put("name", mTextInputName.getText().toString());
            obj.put("lastname", mTextInputLastName.getText().toString());
            obj.put("dni", Integer.parseInt(mTextInputDNI.getText().toString()));
            obj.put("email", mTextInputEmailRegister.getText().toString());
            obj.put("password", mTextInputPasswordRegister.getText().toString());
            obj.put("commission", Integer.parseInt(mTextInputCommission.getText().toString()));

            Intent intentService = new Intent(RegisterActivity.this, ServiceHttpPost.class);
            intentService.putExtra("uri", getString(R.string.uri_register_user));
            intentService.putExtra("jsonData", obj.toString());

            startService(intentService);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void cofigureBroadcastReceiver() {
        filter = new IntentFilter("com.unlam.intentservice.intent.action.RESPONSE_OPERATION");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
    }

    private class ReceiverOperation extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String strJsonData = intent.getStringExtra("jsonData");
                JSONObject jsonData = new JSONObject(strJsonData);

                Log.i("LOG_RECEIVER_OPERATION", "Datos json Main Thread: " + strJsonData);
                Toast.makeText(getApplicationContext(), "Se recibió respuesta del Servidor", Toast.LENGTH_LONG).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
   */
}
