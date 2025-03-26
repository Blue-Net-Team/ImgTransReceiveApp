package app.imgtrans;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class add_server_view extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server_view);

        // 设置顶部工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        // 设置顶部工具栏的标题
        setSupportActionBar(toolbar);
        // 设置顶部工具栏的返回按钮
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // 设置顶部工具栏的返回按钮可见
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // 处理返回按钮事件
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // 添加按钮
        Button addBtn = findViewById(R.id.add_btn);
        EditText ipEdit = findViewById(R.id.ip_address);
        EditText portEdit = findViewById(R.id.port);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取ip和port
                String ip = ipEdit.getText().toString();
                int port = Integer.parseInt(portEdit.getText().toString());
                // 保存到文件
                save2file(ip, port);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * 保存服务器信息到文件
     * @param ip    服务器ip
     * @param port  接收的端口
     */
    public void save2file(String ip, int port){
        try {
            FileOutputStream fos = openFileOutput("server_info", MODE_APPEND);
            // 拼接ip和port ip:port
            String server_info = ip + ":" + port + "\n";
            fos.write(server_info.getBytes());
            fos.close();
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e){
            // 创建文件失败
            Log.e("add_server_view", "save2file: create file failed");
        } catch (IOException e){
            // 写入文件失败
            Log.e("add_server_view", "save2file: write file failed");
        }
    }
}