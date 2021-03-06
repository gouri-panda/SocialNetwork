package com.example.deepak.socialnetworkingapp.conversation;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deepak.socialnetworkingapp.CommentActivity.comment;
import com.example.deepak.socialnetworkingapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.os.SystemClock.sleep;
import static android.widget.Toast.LENGTH_SHORT;

public class coversation extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private static final int LOADER_ID = 22;
    int Uid, Sid;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView progressText;
    private conversation_adapter mAdapter;
    public UpdateMessage updateMessage = new UpdateMessage();
    private ArrayList<conversation_recycler> conversationList = new ArrayList<>();
    private ArrayList<conversation_recycler> conversationListcache = new ArrayList<>();

    private ImageView send_conversation;
    private EditText et_conversation_text;
    public String comparestring= "fhdiuahifhaskdhfk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity_outer);
        Uid = getIntent().getExtras().getInt("Uid");
        Sid = getIntent().getExtras().getInt("Sid");

        recyclerView = (RecyclerView) findViewById(R.id.conversation_recycler_view);


        send_conversation = (ImageView) findViewById(R.id.send_conversation);
        et_conversation_text = (EditText) findViewById(R.id.conversation_text);


        send_conversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Sending message Please wait", LENGTH_SHORT).show();
                MysqlCon mysqlCon = new MysqlCon();
                mysqlCon.execute( "insertMessage",String.valueOf( Uid ),String.valueOf( Sid ),et_conversation_text.getText().toString() );
                et_conversation_text.setText(null);
            }
        });

            prepareconversationData(1);

        conversation_recycler conversation = new conversation_recycler();
        conversation.setConversation_message("hfaiuhdis");
        conversation.setImage("dsfhasfonaso");
        conversation.setProfilename("fahsd8fhiawfiu");
        conversationListcache.add(conversation);


        //Asynk task to update the conversation
            updateMessage.execute();


    }

    @Override
    protected void onDestroy() {
        updateMessage.cancel( true );
        super.onDestroy();
    }

    private void prepareconversationData(int i) {
        //showProgress( true );
        Bundle postQueryBundle = new Bundle( );
        postQueryBundle.putString( "type","getConversation" );
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> postLoader = loaderManager.getLoader( LOADER_ID );
        if(i==1){
            loaderManager.initLoader( LOADER_ID, postQueryBundle, this ).forceLoad();
        }
        else {
            loaderManager.initLoader( LOADER_ID, postQueryBundle, this );
        }

    }

    @Override
    public Loader<String> onCreateLoader(int i, final Bundle bundle) {
        return new AsyncTaskLoader<String>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
            }

            @Override
            public String loadInBackground() {
                String type = bundle.getString( "type" ); //getPostComments
                String con_url = "https://socialnetworkapplication.000webhostapp.com/SocialNetwork/" + type + ".php";
                try {
                    URL url = new URL(con_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);


                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    String post_data = URLEncoder.encode("Uid", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(Uid), "UTF-8") +
                            "&" + URLEncoder.encode("Sid", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(Sid), "UTF-8");


                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"));
                    String result = "";
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                    return result;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String s) {
        String FetchData = s;
        String result = "{\"conversations\":" + FetchData + "}";
        conversationList = parseconversationResult(result);
        if(!(comparestring.equals(conversationList.get(0).getConversation_message()))){
            comparestring = conversationList.get(0).getConversation_message();
        mAdapter = new conversation_adapter(conversationList);
        Context context = getBaseContext();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);}
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    class MysqlCon extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {

            String type = params[0];
            String con_url = "https://socialnetworkapplication.000webhostapp.com/SocialNetwork/" + type + ".php";
            String Uid = params[1];
            String Sid = params[2];
            String message_text = params[3];


            try {
                URL url = new URL(con_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);


                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String post_data = URLEncoder.encode("Uid", "UTF-8") + "=" + URLEncoder.encode(Uid, "UTF-8") + "&"
                        + URLEncoder.encode("Sid", "UTF-8") + "=" + URLEncoder.encode(Sid, "UTF-8") + "&"
                        + URLEncoder.encode("message_text", "UTF-8") + "=" + URLEncoder.encode(message_text, "UTF-8");


                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"));
                String result = "";
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute( s );

            prepareconversationData( 1 );
        }
    }

    class UpdateMessage extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            long time = System.currentTimeMillis()+2000;
            while(time>System.currentTimeMillis());
            UpdateMessage updateMessage = new UpdateMessage();
            updateMessage.execute();
            return "items refreshed";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute( s );
            prepareconversationData( 1 );
        }
    }


    private ArrayList<conversation_recycler> parseconversationResult(String result) {
        ArrayList<conversation_recycler> conversationList = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("conversations");
            for (int i = 0; i < jsonArray.length(); i++) {
                conversation_recycler conversation = new conversation_recycler();
                JSONObject reader = jsonArray.getJSONObject(i);
                conversation.setConversation_message(reader.getString("conversation_text"));
                conversation.setImage(reader.getString("profile"));
                conversation.setProfilename(reader.getString("name"));
                conversationList.add(i,conversation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return conversationList;
    }
    
    
}
