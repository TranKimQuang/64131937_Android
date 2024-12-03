package th.trankimquang.BMICalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText weightNumber, heightNumber;
    private RadioGroup radioGroup;
    private RadioButton asianBtn;
    private Button calBtn;
    private TextView textViewResult, textViewAdvice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ
        weightNumber = findViewById(R.id.weightNumber);
        heightNumber = findViewById(R.id.heightNumber);
        radioGroup = findViewById(R.id.radioCheck);
        asianBtn = findViewById(R.id.asianBtn);
        calBtn = findViewById(R.id.calBtn);
        textViewResult = findViewById(R.id.textViewResult);
        textViewAdvice = findViewById(R.id.textViewAdvice);

        // Nút tính toán
        calBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBMI();
            }
        });
    }

    private void calculateBMI() {
        try {
            // Lấy dữ liệu
            double weight = Double.parseDouble(weightNumber.getText().toString());
            double height = Double.parseDouble(heightNumber.getText().toString());

            // Kiểm tra dữ liệu
            if (height <= 0 || weight <= 0) {
                Toast.makeText(this, "Vui lòng nhập chiều cao và cân nặng hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tính BMI
            double bmi = weight / (height * height);
            String result = String.format("Chỉ số BMI: %.2f", bmi);
            textViewResult.setText(result);

            // Đánh giá dựa trên lựa chọn
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                textViewAdvice.setText("Vui lòng chọn nhóm người để đánh giá!");
                return;
            }

            boolean isAsian = asianBtn.isChecked();
            String advice = getAdvice(bmi, isAsian);
            textViewAdvice.setText(advice);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ dữ liệu!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAdvice(double bmi, boolean isAsian) {
        if (isAsian) {
            if (bmi < 17.5) return "Underweight";
            else if (bmi < 23) return "Normal weight";
            else if (bmi < 28) return "Overweight";
            else return "Obese";
        } else {
            if (bmi < 18) return "Underweight";
            else if (bmi < 25) return "Normal weight ";
            else if (bmi < 30) return "Overweight";
            else return "Obese";
        }
    }
}
