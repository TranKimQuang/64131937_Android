package th.trankimquang.bottomnav;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CN1Fragment()).commit();
        }

        // Handle item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.navigation_cn1) {
                    selectedFragment = new CN1Fragment();
                } else if (id == R.id.navigation_cn2) {
                    selectedFragment = new CN2Fragment();
                } else if (id == R.id.navigation_cn3) {
                    selectedFragment = new CN3Fragment();
                } else if (id == R.id.navigation_cn4) {
                    selectedFragment = new CN4Fragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            }
        });

    }
}
