package th.trankimquang.bottomnav;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Arrays;
import java.util.List;

public class CN3Fragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cn3, container, false);

        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.horizontal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Data for RecyclerView
        List<String> data = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4");

        // Set adapter
        CN3Adapter adapter = new CN3Adapter(data);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
