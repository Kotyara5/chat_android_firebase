package com.bc.chatbc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseListAdapter;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MainActivity extends AppCompatActivity {
    //Сохранение и загрузка данных с устройства
    SharedPreferences sPref;

    //Основные поля
    private LinearLayout signIn_VerLayout, register_VerLayout, chat_VerLayout, friends_VerLayout, messages_Layout;
    private CheckBox signIn_checkBox;
    private ListView list_of_friends, list_of_dialogs, list_of_all_users;

    //Отображение друзей пользователя
    ArrayList<User> ArrayFriends = new ArrayList<>(); //Массив профилей друзей текущего пользователя
    ValueEventListener listenerFriends; //Слушатель изменений в списке друзей текущего пользователя (в бд)

    //Отображение диалогов пользователя
    ArrayList<Dialog> ArrayDialogs = new ArrayList<>(); //Массив диалогов текущего пользователя
    ArrayList<String> idOfDialogs = new ArrayList<>(); //Массив ИД диалогов текущего пользователя
    ValueEventListener listenerDialogs; //Слушатель изменений в списке диалогов текущего пользователя (в бд)

    //Окно переписки
    private ImageButton sendButton; //Кнопка отправки сообщения
    private EmojiconEditText emojiconEditText; //Окно ввода текста и смайлов

    //Для подключения к бд
    private FirebaseAuth db_auth;
    private DatabaseReference ref_users, ref_dialogs; //Расположение всех пользователей и всех диалогов в бд

    //Данные профиля
    private String thisUserName; //Имя текущего пользователя

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        db_auth = FirebaseAuth.getInstance();
        ref_users = FirebaseDatabase.getInstance().getReference("Users");
        ref_dialogs = FirebaseDatabase.getInstance().getReference("Dialogs");

        signIn_VerLayout = findViewById(R.id.signIn_VerLayout);
        register_VerLayout = findViewById(R.id.register_VerLayout);
        chat_VerLayout = findViewById(R.id.chat_VerLayout);
        messages_Layout = findViewById(R.id.messages_Layout);
        friends_VerLayout = findViewById(R.id.friends_VerLayout);

        list_of_dialogs = findViewById(R.id.list_of_dialogs);
        list_of_friends = findViewById(R.id.list_of_friends);
        list_of_all_users = findViewById(R.id.list_of_all_users);

        //Слушатель изменения списка друзей пользователя
        listenerFriends = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Если у пользователя есть друзья
                if (snapshot.child(db_auth.getCurrentUser().getUid() + "/Friends").exists()) {
                    ArrayFriends.clear();
                    idOfDialogs.clear();
                    //Взять всех друзей из бд
                    for (DataSnapshot ds : snapshot.child(db_auth.getCurrentUser().getUid() + "/Friends").getChildren()) {
                        String idFriend = ds.getKey(); //Взять ид(ключ) друга
                        User friend = snapshot.child(idFriend).getValue(User.class); //Взять друга по ид (ключу) из списка всех пользователей
                        ArrayFriends.add(friend); //Добавить в массив друзей пользователя
                        String idDialog = ds.getValue(String.class); //Так же взять ид (ключ) диалога, который связан с другом пользователя
                        idOfDialogs.add(idDialog); //Добавить в массив ид (ключей) диалогов текущего пользователя
                    }
                    //Связать массив друзей пользователя с отображаемым списком друзей
                    AdapterUser adapterUser = new AdapterUser(getApplicationContext(), ArrayFriends);
                    list_of_friends.setAdapter(adapterUser);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast toast = Toast.makeText(getApplicationContext(), "Отображение друзей не удалось: " + error.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }
        };

        //Слушатель изменения списка диалогов пользователя
        listenerDialogs = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Если у пользователя есть диалоги
                if (snapshot.exists()) {
                    ArrayDialogs.clear();
                    //Взять из всех диалогов только те, ид (ключи) которых записаны в профиле пользователя
                    for (String idDialog : idOfDialogs) {
                        Dialog dialog = snapshot.child(idDialog).getValue(Dialog.class);
                        ArrayDialogs.add(dialog); //Добавляем каждый диалог в массив диалогов
                    }
                    //Связать массив диалогов пользователя с отображаемым списком диалогов
                    AdapterDialog adapterDialog = new AdapterDialog(getApplicationContext(), ArrayDialogs, thisUserName);
                    list_of_dialogs.setAdapter(adapterDialog);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast toast = Toast.makeText(getApplicationContext(), "Отображение диалогов не удалось: " + error.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }
        };

        //Окно переписки
        sendButton = findViewById(R.id.sendButton);
        emojiconEditText = findViewById(R.id.textField);
        EmojIconActions emojIconActions = new EmojIconActions(getApplicationContext(), findViewById(R.id.main_layout), emojiconEditText, findViewById(R.id.emoji_Button));
        emojIconActions.ShowEmojIcon();

        //Чтение файла с устройства
        sPref = getSharedPreferences("MainData", MODE_PRIVATE);
        boolean m_isRememberUser = sPref.getBoolean("isRememberUser", false);

        //Если установлена настройка "Запомнить пользователя", то войти автоматически с сохранёнными данными
        if (m_isRememberUser) {
            String m_email = sPref.getString("email", "@a");
            String m_pass = sPref.getString("pass", "11");
            signInChat(m_email, m_pass);
        } else { //Иначе отобразить окно входа
            showSignInWindow();
        }

        //Кнопки регистрации и авторизации
        Button register_Button = findViewById(R.id.register_button);
        Button signIn_Button = findViewById(R.id.signIn_button);
        signIn_checkBox = findViewById(R.id.signIn_checkBox);
        register_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterWindow();
            }
        });
        signIn_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Проверка заполненности полей
                MaterialEditText email = findViewById(R.id.emailField_SignIn);
                MaterialEditText pass = findViewById(R.id.passwordField_SignIn);
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Введите email", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (TextUtils.isEmpty(pass.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                signInChat(email.getText().toString(), pass.getText().toString());
            }
        });

        //Кнопки для переключения между меню друзей и диалогов
        Button dialogs_button = findViewById(R.id.dialogs_button);
        Button friends_button = findViewById(R.id.friends_button);
        dialogs_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friends_VerLayout.setVisibility(View.INVISIBLE);
                list_of_dialogs.setVisibility(View.VISIBLE);
            }
        });
        friends_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friends_VerLayout.setVisibility(View.VISIBLE);
                list_of_dialogs.setVisibility(View.INVISIBLE);
            }
        });

        //Кнопка выхода из текущего аккаунта
        Button exit_button = findViewById(R.id.exit_button);
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_auth.signOut();
                showSignInWindow();
            }
        });

        //Кнопки для переключения между списоком всех пользователей и только друзьями
        Button only_my_friends_button = findViewById(R.id.only_my_friends_button);
        Button all_users_button = findViewById(R.id.all_users_button);
        only_my_friends_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_of_all_users.setVisibility(View.INVISIBLE);
                list_of_friends.setVisibility(View.VISIBLE);
            }
        });
        all_users_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_of_all_users.setVisibility(View.VISIBLE);
                list_of_friends.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showSignInWindow() {
        signIn_VerLayout.setVisibility(View.VISIBLE);
        chat_VerLayout.setVisibility(View.INVISIBLE);
        register_VerLayout.setVisibility(View.INVISIBLE);
    }

    private void signInChat(String email, String pass){
        db_auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Если при входе была установлена настройка "Запомнить пользователя", то данные входа записываются на устройство
                        if (signIn_checkBox.isChecked()) {
                            sPref = getSharedPreferences("MainData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sPref.edit();
                            editor.putString("email", email);
                            editor.putString("pass", pass);
                            editor.putBoolean("isRememberUser", true);
                            editor.apply();
                        }

                        //Взять из бд имя текущего пользователя
                        ref_users.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                thisUserName = snapshot.child(db_auth.getCurrentUser().getUid()).getValue(User.class).getName();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast toast = Toast.makeText(getApplicationContext(), "Подключение к профилю не удалось: " + error.getMessage(), Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 0);
                                toast.show();
                            }
                        });

                        //Отобразить основной экран приложения с диалогами и друзьями
                        displayFriendsAndDialogs();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Если авторизация не удалась, то показать ошибку и окно входа
                        Toast toast = Toast.makeText(getApplicationContext(), "Ошибка авторизации: " + e.getMessage(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                        showSignInWindow();
                    }
                });
    }

    private void showRegisterWindow() {
        signIn_VerLayout.setVisibility(View.INVISIBLE);
        register_VerLayout.setVisibility(View.VISIBLE);

        final MaterialEditText email = findViewById(R.id.emailField_Register);
        final MaterialEditText pass = findViewById(R.id.passwordField_Register);
        final MaterialEditText passToo = findViewById(R.id.passwordToo_Field_Register);
        final MaterialEditText name = findViewById(R.id.userNameField_Register);

        Button register = findViewById(R.id.register_buttonInReg);
        Button cancel = findViewById(R.id.cancel_button);
        //Выйти из окна регистрации в окно авторизации
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInWindow();
                email.setText("");
                pass.setText("");
                passToo.setText("");
                name.setText("");
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Проверка правильности заполненности полей регистрации
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Поле email не заполнено", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (pass.getText().toString().length() < 6) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Пароль должен иметь 6 или более символов", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (!passToo.getText().toString().equals(pass.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Пароли не совпадают", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (TextUtils.isEmpty(name.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Поле имя не заполнено", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                //Регистрация пользователя в бд
                db_auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //Создать структуру пользователя
                                User user = new User(db_auth.getCurrentUser().getUid(), email.getText().toString(), pass.getText().toString(), name.getText().toString());

                                //Добавить пользователя в список существующих пользователей
                                ref_users.child(user.getId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Пользователь добавлен", Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.TOP, 0, 0);
                                        toast.show();

                                        //Перейти в окно авторизации
                                        showSignInWindow();
                                        email.setText("");
                                        pass.setText("");
                                        passToo.setText("");
                                        name.setText("");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Пользователь не добавлен в бд: " + e.getMessage(), Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.TOP, 0, 0);
                                        toast.show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Пользователь не зарегистрирован: " + e.getMessage(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                    }
                });
            }
        });
    }

    private void displayFriendsAndDialogs() {
        signIn_VerLayout.setVisibility(View.INVISIBLE);
        chat_VerLayout.setVisibility(View.VISIBLE);
        friends_VerLayout.setVisibility(View.INVISIBLE);
        list_of_dialogs.setVisibility(View.VISIBLE);

        //Установить слушатель для изменений в списке друзей текущего пользователя
        ref_users.addValueEventListener(listenerFriends);

        //Отобразить всех пользователей
        FirebaseListAdapter<User> adapterUser = new FirebaseListAdapter<User>(this, User.class, R.layout.user_list_item, ref_users) {
            @Override
            protected void populateView(View v, User model, int position) {
                TextView name_user, email_user;
                name_user = v.findViewById(R.id.textNameUserFriend);
                email_user = v.findViewById(R.id.textEmailUserFriends);

                name_user.setText(model.getName());
                email_user.setText(model.getEmail());
            }
        };
        list_of_all_users.setAdapter(adapterUser);

        //Нажатие по пользователю в списке всех пользователей
        //Данное действие добавит пользователя в друзья текущего пользователя или покажет ошибку с информацией, почему пользователя невозможно добавить в друзья
        list_of_all_users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = adapterUser.getItem(position); //Получаем выбранного пользователя
                String idUser = user.getId(); //Получаем ид (ключ) выбранного пользователя
                //Если пользователь нажал на себя
                if (idUser.equals(db_auth.getCurrentUser().getUid())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Вы не можете добавить себя в друзья", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                //Заходим в список друзей текущего пользователя в бд
                ref_users.child(db_auth.getCurrentUser().getUid() + "/Friends").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //Проверяем, есть ли пользователь уже в друзьях
                            boolean isFriend = false;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String idFriend = ds.getKey();
                                if (idUser.equals(idFriend)) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Пользователь уже в друзьях", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP, 0, 0);
                                    toast.show();
                                    isFriend = true;
                                }
                            }
                            //Если пользователя в друзьях нет, то добавляем в друзья
                            if (!isFriend) addNewFriend(idUser);
                        //Если у текущего пользователя нет списка друзей, то сразу добавляем
                        } else { addNewFriend(idUser); }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //Установить слушатель для изменений в списке диалогов текущего пользователя
        ref_dialogs.addValueEventListener(listenerDialogs);

        //Нажатие по диалогу откроет выбранный диалог
        list_of_dialogs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String idOpenDialog = idOfDialogs.get(position);
                displayAllMessages(idOpenDialog);
            }
        });
    }

    private void addNewFriend(String idNewFriend){
        ref_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Берём профили пользователей, которые буду добавлены в друзья
                    User friend = snapshot.child(idNewFriend).getValue(User.class);
                    User thisUser = snapshot.child(db_auth.getCurrentUser().getUid()).getValue(User.class);
                    //Создаём в бд место с ключом под новый диалог
                    DatabaseReference ref = ref_dialogs.push();
                    String key = ref.getKey();

                    //ВАЖНО!: необходимо сначала добавить пользователей в друзья, а потом добавить диалог,
                    // так как слушателю изменений диалога требуется информация от слушателя изменений друзей, который должен быть вызван первым

                    //Добавляем в друзья и добавляем ид (ключ) диалога в профиле каждого друга
                    thisUser.addFriendsAndDialogs(friend.getId(), key);
                    friend.addFriendsAndDialogs(thisUser.getId(), key);

                    //Создаём диалог в бд и добавляем первое сообщение от добавившего друга
                    ref_dialogs.child(key).setValue(new Dialog(friend.getName(), thisUser.getName()));
                    addNewMessage(key, thisUser.getName(), "Я начал диалог!");

                    Toast toast = Toast.makeText(getApplicationContext(), "Пользователь добавлен в друзья", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast toast = Toast.makeText(getApplicationContext(), "Добавление друга не удалось: " + error.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }
        });
    }

    private void displayAllMessages(String idOpenDialog) {
        chat_VerLayout.setVisibility(View.INVISIBLE);
        messages_Layout.setVisibility(View.VISIBLE);

        //Кнопка отправления сообщения
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Если поле пустое, то ничего не происходит
                if (TextUtils.isEmpty(emojiconEditText.getText().toString())) return;
                //Иначе добавляется новое сообщение
                addNewMessage(idOpenDialog, thisUserName, emojiconEditText.getText().toString());

                emojiconEditText.setText("");
            }
        });

        //Адаптер для отображения списка сообщений
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        FirebaseListAdapter<Message> adapterMessage = new FirebaseListAdapter<Message>(this, Message.class, R.layout.message_list_item, ref_dialogs.child(idOpenDialog + "/Messages")) {
            @Override
            protected void populateView(View v, Message model, int position) {
                TextView msg_user, msg_time;
                BubbleTextView msg_text;
                LinearLayout VerLayout_item_message;
                VerLayout_item_message = v.findViewById(R.id.message_BubbleLayout);
                msg_user = v.findViewById(R.id.textNameUser);
                msg_text = v.findViewById(R.id.textMessage);
                msg_time = v.findViewById(R.id.textDate);

                //Сообщения текущего пользователя справа, а сообщения друга слева
                if(model.getUserName().equals(thisUserName)) {
                    VerLayout_item_message.setGravity(Gravity.RIGHT);
                } else {
                    VerLayout_item_message.setGravity(Gravity.LEFT);
                }

                msg_user.setText(model.getUserName());
                msg_text.setText(model.getTextMessage());
                msg_time.setText(DateFormat.format("dd-MM-yyyy HH:mm:ss", model.getTimeMessage()));
            }
        };
        listOfMessages.setAdapter(adapterMessage);
    }

    public void addNewMessage(String idOfDialog, String userName, String textMessage) {
        DatabaseReference ref_messages = FirebaseDatabase.getInstance().getReference("Dialogs/" + idOfDialog + "/Messages");
        ref_messages.push().setValue(new Message(userName, textMessage));
        //Обновить последнее сообщение для отображения в списке диалогов
        FirebaseDatabase.getInstance().getReference("Dialogs/" + idOfDialog).child("lastMessage").setValue(textMessage);
    }
    
    @Override
    public void onBackPressed() {
        //Если открыт диалог, то перейти в окно списка диалогов
        if (messages_Layout.getVisibility() == View.VISIBLE) {
            messages_Layout.setVisibility(View.INVISIBLE);
            chat_VerLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}