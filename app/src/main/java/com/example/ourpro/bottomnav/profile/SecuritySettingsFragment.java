package com.example.ourpro.bottomnav.profile;

import static androidx.fragment.app.FragmentManager.TAG;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.renderscript.ScriptGroup;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ourpro.R;
import com.example.ourpro.databinding.FragmentSecuritySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class SecuritySettingsFragment extends Fragment {

    public static final String ARG_SELECTED_VALUE = "selected_value";
    private static final String TAG = "Upload ###";

    private FragmentSecuritySettingsBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Инфлейтим layout для этого фрагмента
        return inflater.inflate(R.layout.fragment_security_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentSecuritySettingsBinding.bind(view);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Получаем аргументы
        Bundle args = getArguments();

        binding.settingButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.menu_fr, new AccountSettingFragment()) // Заменяем текущий фрагмент
                    .addToBackStack(null) // Добавляем в back stack для возможности возврата
                    .commit();
        });

        if (args != null && args.containsKey(ARG_SELECTED_VALUE)) {
            int value = args.getInt(ARG_SELECTED_VALUE);

            switch (value) {
                case 1:
                    // Действия для смены пароля
                    binding.infText.setText("Точно ли хотите поменять пароль?");

                    break;
                case 2:
                    // Действия для смены email
                    binding.infText.setText("Точно ли хотите поменять почту?");
                    binding.newPosition.setHint("Новая почта");
                    binding.newPasswordRep.setVisibility(View.INVISIBLE);
                    binding.newPosition.setEndIconMode(END_ICON_NONE);
                    binding.newPositionGet.setTransformationMethod(null);
                    loadEmail();
                    binding.saveSafety.setOnClickListener(v ->{
                        loadEmail();
                    });

                    break;
                case 3:
                    // Действия для смены имени пользователя
                    binding.infText.setText("Точно ли хотите поменять имя пользователя?");
                    binding.newPosition.setHint("Ваше имя пользователя");
                    binding.newPasswordRep.setVisibility(View.INVISIBLE);
                    binding.newPosition.setEndIconMode(END_ICON_NONE);
                    binding.newPositionGet.setTransformationMethod(null);
                    loadUsername();
                    binding.saveSafety.setOnClickListener(v -> {
                        saveUsername();
                    });

                    break;
            }
        } else {
        //    textView.setText("Значение не передано");
            Log.e("SecuritySettings", "Аргументы отсутствуют!");
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Переход в Личный кабинет
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.menu_fr, new ProfileFragment())
                                .commit();
                    }
                }
        );

    }


    ///Смена эл.почты
    //Выгрузка эл.почты
    private void loadEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Ошибка: пользователь не найден");
            return;
        }

        String userId = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prowise-de1d0-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference databaseUserName = database.getReference("Users").child(userId).child("email");
        databaseUserName.get().addOnSuccessListener(emailBase -> {
            if (emailBase.exists() && emailBase.getValue() != null) {
                String email = emailBase.getValue(String.class);
                if (email != null) {
                    binding.newPositionGet.setText(email);
                    Log.d(TAG, "Ник загружено: " + email);
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Ошибка загрузки ник: " + e.getMessage()));
    }

    //Загрузка эл.почты
    private void saveEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://prowise-de1d0-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("Users");

            String email = binding.newPositionGet.getText().toString();
            Map<String, Object> userUpdates = new HashMap<>();
            checkFieldAvailability("email", email, isAvailable -> {
                if (isAvailable) {
                    userUpdates.put("email", email);
                    databaseReference.child(userId).updateChildren(userUpdates)
                            .addOnSuccessListener(aVoid -> Log.d("TAG", "email сохранен в Firebase"))
                            .addOnFailureListener(e -> Log.e("TAG", "Ошибка сохранения в Firebase: " + e.getMessage()));
                } else {
                    Toast.makeText(requireContext(), "Этот email уже зарегистрирован", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }




    ///Смена имени пользователя
    //Загрузка имени пользователя
    private void saveUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://prowise-de1d0-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("Users");

            String username = binding.newPositionGet.getText().toString().trim();
            Map<String, Object> userUpdates = new HashMap<>();

            // вызываем общий метод, передавая поле "username"
            checkFieldAvailability("username", username, isAvailable -> {
                if (isAvailable) {
                    userUpdates.put("username", username);
                    databaseReference.child(userId).updateChildren(userUpdates)
                            .addOnSuccessListener(aVoid -> Log.d("TAG", "username сохранён в Firebase"))
                            .addOnFailureListener(e -> Log.e("TAG", "Ошибка сохранения username: " + e.getMessage()));
                } else {
                    Toast.makeText(requireContext(), "Этот username уже занят", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //выгрузка имени пользователя
    private void loadUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Ошибка: пользователь не найден");
            return;
        }

        String userId = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prowise-de1d0-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference databaseUserName = database.getReference("Users").child(userId).child("username");
        databaseUserName.get().addOnSuccessListener(UserNameBase -> {
            if (UserNameBase.exists() && UserNameBase.getValue() != null) {
                String username = UserNameBase.getValue(String.class);
                if (username != null) {
                    binding.newPositionGet.setText(username);
                    Log.d(TAG, "Ник загружено: " + username);
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Ошибка загрузки ник: " + e.getMessage()));
    }




    private void checkFieldAvailability(String fieldName, String value, OnFieldCheckListener listener) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://prowise-de1d0-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Users");

        usersRef.orderByChild(fieldName).equalTo(value).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isAvailable = !task.getResult().hasChildren(); // true — свободно
                        listener.onCheck(isAvailable);
                    } else {
                        listener.onCheck(false); // В случае ошибки считаем занятым
                    }
                });
    }


    // Callback-интерфейс для обработки результата
    public interface OnFieldCheckListener {
        void onCheck(boolean isAvailable);
    }





}



