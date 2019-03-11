package de.tudarmstadt.informatik.pet.pet2018client;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class TransActivity extends AppCompatActivity {

    private Button ok;
    private EditText address;
    private EditText filename;

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);
        ok = findViewById(R.id.ok);
        address = findViewById(R.id.serveradd);
        filename = findViewById(R.id.filename);
        final String base_url = "http://";

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String add = String.valueOf(address.getText());
//                String add = "172.18.85.154";
                String name = String.valueOf(filename.getText());
//                String name = "TEST/humans.txt";
                String url = base_url + add + "/upload";
                String filepath = Environment.getExternalStorageDirectory().toString() + "/" + name;
                SendFile(url, filepath);

            }
        });
    }

    private void SendFile(String url, String filepath)
    {
        MediaType TxtFile = MediaType.parse("text/plain; charset=utf-8");
        MediaType AudFile = MediaType.parse("audio/wav");
        MediaType Image = MediaType.parse("image/jpeg");
        MediaType unknown;
        String subString = filepath.substring(filepath.length()-3);
        if (subString == "txt")
        {
            unknown = TxtFile;
        }
        else if (subString == "wav")
        {
            unknown = AudFile;
        }
        else
        {
            unknown = Image;
        }
        File file = new File(filepath);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(unknown, file))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TransActivity.this, "Server Wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (res.equals("0"))
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TransActivity.this,"already registered",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TransActivity.this,"succeed",Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                });
            }
        });
    }
}
