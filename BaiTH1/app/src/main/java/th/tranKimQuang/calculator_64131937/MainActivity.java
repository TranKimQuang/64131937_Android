package th.tranKimQuang.calculator_64131937;

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

    private EditText aNumber;
    private EditText bNumber;
    private RadioGroup radioGroup;
    private Button buttonCalculate;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần giao diện
        aNumber = findViewById(R.id.aNumber);
        bNumber = findViewById(R.id.bNumber);
        radioGroup = findViewById(R.id.radioGroup);
        buttonCalculate = findViewById(R.id.buttonCalculate);
        textViewResult = findViewById(R.id.textViewResult);

        // Xử lý sự kiện nút tính toán
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateResult();
            }
        });
    }

    private void calculateResult() {
        try {
            // Lấy giá trị từ EditText
            String aStr = aNumber.getText().toString().trim();
            String bStr = bNumber.getText().toString().trim();

            if (aStr.isEmpty() || bStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ số A và B", Toast.LENGTH_SHORT).show();
                return;
            }

            double a = Double.parseDouble(aStr);
            double b = Double.parseDouble(bStr);

            // Lấy phép tính từ RadioGroup
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Vui lòng chọn phép tính", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = findViewById(selectedId);
            String operation = selectedRadioButton.getText().toString();

            double result;
            switch (operation) {
                case "+":
                    result = a + b;
                    break;
                case "-":
                    result = a - b;
                    break;
                case "*":
                    result = a * b;
                    break;
                case "/":
                    if (b == 0) {
                        Toast.makeText(this, "Không thể chia cho 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    result = a / b;
                    break;
                default:
                    Toast.makeText(this, "Phép tính không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
            }

            // Hiển thị kết quả
            textViewResult.setText(String.format("Kết quả: %.2f", result));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
