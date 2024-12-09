package th.trankimquang.fragment;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;

import th.trankimquang.fragment.English.EngFragment;
import th.trankimquang.fragment.IT.ITFragment;
import th.trankimquang.fragment.Math.MathFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mathbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(new MathFragment());
            }
        });

        findViewById(R.id.engbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(new EngFragment());
            }
        });

        findViewById(R.id.ITbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(new ITFragment());
            }
        });
    }

    private void showFragment(Fragment fragment) {
        // Hide main UI elements
        findViewById(R.id.title).setVisibility(View.GONE);
        findViewById(R.id.mathbtn).setVisibility(View.GONE);
        findViewById(R.id.engbtn).setVisibility(View.GONE);
        findViewById(R.id.ITbtn).setVisibility(View.GONE);
        findViewById(R.id.title2).setVisibility(View.GONE);

        // Show the fragment container and replace the fragment
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Optional: to add the transaction to the back stack
        transaction.commit();
    }
}
