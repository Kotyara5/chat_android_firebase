package com.bc.chatbc;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterDialog extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Dialog> objects;
    String nameUser;

    AdapterDialog(Context context, ArrayList<Dialog> Dialogs, String nameUser) {
        this.nameUser = nameUser;
        ctx = context;
        objects = Dialogs;
        lInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.dialog_list_item, parent, false);
        }

        Dialog model = ((Dialog) getItem(position));

        // заполняем View в пункте списка
        TextView name_user, email_user;
        name_user = view.findViewById(R.id.textNameUser);
        email_user = view.findViewById(R.id.textLastMessage);

        if (model.getNameUser().equals(nameUser))
            name_user.setText(model.getNameFriend());
        else
            name_user.setText(model.getNameUser());
        email_user.setText(model.getLastMessage());

        return view;
    }
}
