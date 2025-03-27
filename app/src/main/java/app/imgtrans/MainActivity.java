package app.imgtrans;

import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import app.imgtrans.receiver.Receiver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Button addBtn;

    // 用来存储 MenuItem 的 ID 和对应的唯一 ID
    private Map<Integer, String> menuItemIdToUniqueIdMap = new HashMap<>();

    private LinearLayout menuContainer;

    // 文件监控
    private FileObserver fileObserver;

    // 关闭连接按钮
    private Button closeBtn;

    // 删除按钮
    private Button delBtn;

    private Receiver rec = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        // XXX: 删除文件
//        deleteFile("server_info");

        // 如果不存在server_info文件，创建一个
        if (!getFileStreamPath("server_info").exists()) {
            try {
                FileOutputStream fos = openFileOutput("server_info", MODE_PRIVATE);
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "onCreate: create file failed");
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        addBtn = findViewById(R.id.add_btn);
        menuContainer = findViewById(R.id.menu_container);
        ImageView imageView = findViewById(R.id.imageView);
        closeBtn = findViewById(R.id.close);
        delBtn = findViewById(R.id.del_btn);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 侧边栏的点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (rec != null) {
                    // 关闭连接
                    rec.close();
                }
                // 提取item的title
                String title = Objects.requireNonNull(item.getTitle()).toString();

                String ip = title.split(":")[0];
                int port = Integer.parseInt(title.split(":")[1]);

                rec = new Receiver(imageView, ip, port);
                rec.startReceiving();
                return true;
            }
        });

        // 添加按钮的点击跳转
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, add_server_view.class);
                startActivity(intent);
            }
        });

        // 删除按钮的点击跳转
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, del_server_view.class);
                startActivity(intent);
            }
        });

        // 关闭连接按钮
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rec != null) {
                    rec.close();
                } else {
                    Toast.makeText(MainActivity.this, "未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 检查文件是否为空
        List<String> menuItems = readMenuFromFile();
        if (menuItems == null || menuItems.isEmpty()) {
            showEmptyMenuMessage(); // 显示空菜单提示
        } else {
            // 动态加载菜单项
            loadMenuItems();
        }

        // 开始监听文件变化
        startFileObserver();
    }

    /**
     * 如果文件为空，显示提示信息
     */
    private void showEmptyMenuMessage() {
        // 清空容器（如果之前有任何菜单项）
        menuContainer.removeAllViews();

        // 创建一个新的 TextView
        TextView textView = new TextView(this);
        textView.setText("没有添加任何服务器信息");
        textView.setTextSize(16);
        // 居中显示
        textView.setGravity(Gravity.CENTER);
        // 垂直居中
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // 将 TextView 添加到容器中
        menuContainer.addView(textView);
    }

    /**
     * 读取文件并动态加载菜单项
     */
    public void loadMenuItems() {
        List<String> menuItems = readMenuFromFile();  // 读取文件
        // 获取 NavigationView 的 Menu
        Menu menu = navigationView.getMenu();
        menu.clear();  // 清空现有的菜单项（如果有）
        if (menuItems.isEmpty()){
            menuContainer.removeAllViews();
            showEmptyMenuMessage();
        }
        if (menuItems != null && !menuItems.isEmpty()) {
            // 遍历菜单项并动态添加
            for (int i = 0; i < menuItems.size(); i++) {
                // 生成唯一的ID
                String serverInfo = menuItems.get(i);  // 例如 "192.168.13.1:1234"
                String uniqueId = generateIdFromServerInfo(serverInfo);  // 转换为 "192168013001_1234"

                // 给每个菜单项分配唯一的 itemId
                int itemId = Menu.FIRST + i;  // 用索引来确保唯一ID
                MenuItem menuItem = menu.add(Menu.NONE, itemId, Menu.NONE, serverInfo);

                // 将 itemId 和对应的唯一 ID 存储到 Map 中
                menuItemIdToUniqueIdMap.put(itemId, uniqueId);
            }
        }
    }

    /**
     * 根据服务器信息生成唯一的 ID
     * 格式：将 IP 地址中的点去掉，端口号保持不变，形成类似 "192168013001_1234" 的格式
     */
    public String generateIdFromServerInfo(String serverInfo) {
        // 将 IP 地址和端口分开
        String[] parts = serverInfo.split(":");
        if (parts.length == 2) {
            String ip = parts[0].replace(".", "");  // 将 IP 地址中的点替换为空
            String port = parts[1];  // 端口号
            return ip + "_" + port;  // 生成 ID 格式 "192168013001_1234"
        }
        return null;
    }

    /**
     * 读取文件内容并返回菜单项列表
     */
    public List<String> readMenuFromFile() {
        List<String> menuItems = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput("server_info");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                menuItems.add(line);
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "readMenuFromFile: read file failed");
        }
        return menuItems;
    }

    /**
     * 启动文件监控
     */
    private void startFileObserver() {
        String filePath = getFilesDir() + "/server_info";  // 获取文件路径
        fileObserver = new FileObserver(filePath, FileObserver.MODIFY) {
            @Override
            public void onEvent(int event, String path) {
                // 只有当文件修改时才执行
                if (event == FileObserver.MODIFY) {
                    Log.i(TAG, "File modified, updating menu.");
                    // 文件修改后重新加载菜单
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            menuContainer.removeAllViews();
                            loadMenuItems();
                        }
                    });
                }
            }
        };
        fileObserver.startWatching();  // 开始监控
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fileObserver != null) {
            fileObserver.stopWatching();  // 停止监控
        }
    }
}
