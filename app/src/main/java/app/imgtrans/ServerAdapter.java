package app.imgtrans;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.model.Server;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {

    private List<Server> serverList;

    public ServerAdapter(List<Server> serverList) {
        this.serverList = serverList;
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_server, parent, false);
        return new ServerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        Server server = serverList.get(position);
        holder.serverInfo.setText(server.Ip + ":" + server.Port);
        holder.serverCheckbox.setChecked(server.isSelected);
        holder.serverCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> server.isSelected = isChecked);
    }

    @Override
    public int getItemCount() {
        return serverList.size();
    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder {
        CheckBox serverCheckbox;
        TextView serverInfo;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            serverCheckbox = itemView.findViewById(R.id.server_checkbox);
            serverInfo = itemView.findViewById(R.id.server_info);
        }
    }
}