package th.trankimquang.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EngFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eng, container, false);

        // Add TitleFragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentTitle, new TitleFragment());
        transaction.replace(R.id.fragmentQuestion, new QuestionFragment());
        transaction.replace(R.id.fragmentButton, new BtnFragment());
        transaction.commit();

        return view;
    }
}
