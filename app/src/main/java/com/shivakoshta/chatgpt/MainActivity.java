package com.shivakoshta.chatgpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    EditText sendMessage;
    ImageButton sendButton;
    RecyclerView recyclerView;
    TextView welcomeText;
    List<Message_model> messageList;
    Chat_Adapter chat_adapter;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "ChatGPT Clone By Shiva", Toast.LENGTH_LONG).show();
        messageList = new ArrayList<>();
        sendMessage = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        welcomeText = findViewById(R.id.welcomeText);
        recyclerView = findViewById(R.id.recycler_view);
        //setting the recyclerview
        chat_adapter = new Chat_Adapter(messageList);
        recyclerView.setAdapter(chat_adapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        addResponse("Hi there! How can i help you?");
        welcomeText.setVisibility(View.GONE);

        sendButton.setOnClickListener((v) -> {
            String question = sendMessage.getText().toString().trim();
            if(question.length()!=0)
            {
                addToChat(question, Message_model.SENT_BY_ME);
                sendMessage.setText("");
                callAPI(question);
                welcomeText.setVisibility(View.GONE);
            }

        });
    }

    void addToChat(String message, String SentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message_model(message, SentBy));
                chat_adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(chat_adapter.getItemCount());
            }
        });
    }

    void addResponse(String response)
    {
        if(messageList.size()!=0)
            messageList.remove(messageList.size()-1);
        addToChat(response,Message_model.SENT_BY_BOT);
    }

void callAPI(String question){
    //okhttp
    messageList.add(new Message_model("Typing... ",Message_model.SENT_BY_BOT));

    JSONObject jsonBody = new JSONObject();
    try {
//        jsonBody.put("model","text-davinci-003");
//        jsonBody.put("prompt",question);
//        jsonBody.put("max_tokens",4000);
//        jsonBody.put("temperature",0);
        jsonBody.put("model","gpt-3.5-turbo");
        jsonBody.put("user","Shiva07619981");
        JSONArray MsgArray = new JSONArray();

        JSONObject obj = new JSONObject();
        obj.put("role","user");
        obj.put("content",question);
        MsgArray.put(obj);
        jsonBody.put("messages",MsgArray);
    } catch (JSONException e) {
        e.printStackTrace();
    }
    RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
    Request request = new Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization","Bearer API_KEY_HERE")
            .post(body)
            .build();
    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            addResponse("Failed to load response due to "+e.getMessage());
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if(response.isSuccessful()){
                JSONObject  jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = jsonObject.getJSONArray("choices");
                    String result = jsonArray.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    addResponse(result.trim());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                addResponse("Failed to load response due to "+response.body().toString());
            }
        }
    });
}
}