package app.imgtrans;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.model.Server;

public class del_server_view extends AppCompatActivity {

    private static final String TAG = "del_server_view";

    // 服务器列表
    private List<Server> serverList = new ArrayList<>();

    // 服务器列表视图
    private RecyclerView recyclerView;

    // 服务器适配器
    private ServerAdapter serverAdapter;

    // 删除按钮
    private Button delBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_del_server_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        serverList = readServer();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        serverAdapter = new ServerAdapter(serverList);
        recyclerView.setAdapter(serverAdapter);

        delBtn = findViewById(R.id.del_selected);
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < serverList.size(); i++) {
                    if (serverList.get(i).isSelected) {
                        serverList.remove(i);
                        i--;
                    }
                }
                try {
                    FileOutputStream fos = openFileOutput("server_info", MODE_PRIVATE);
                    for (Server server : serverList) {
                        String info = server.Ip + ":" + server.Port + "\n";
                        fos.write(info.getBytes());
                    }
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "onClick: write file failed");
                }
                serverAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 读取服务器信息
     * @return 服务器信息列表
     */
    public List<Server> readServer(){
        List<Server> serverList = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput("server_info");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                String[] info = line.split(":");
                Server server = new Server(info[0], Integer.parseInt(info[1]), false);
                serverList.add(server);
            }
            br.close();
            fis.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "readServer: file not found");
        } catch (IOException e) {
            Log.e(TAG, "readServer: read file failed");
        }
        return serverList;
    }
}