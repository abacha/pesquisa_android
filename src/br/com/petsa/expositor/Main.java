package br.com.petsa.expositor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {
	/** Called when the activity is first created. */

	private enum QuestionType {
		MULTIPLE, UNIQUE, GRADE, OPEN, ORDER
	};

	private Integer SURVEY;
	private final String URL = "http://studioblackdog.com/pesquisa/submit.php";
	private static Integer questionNumber = 0;
	private int QUESTIONS;
	private final String NEXT_QUESTION = "Próxima";
	private final String BACK_QUESTION = "Anterior";
	private final String END_FORM = "Finalizar";

	private Map<Integer, Map<Integer, String>> formAnswers = new HashMap<Integer, Map<Integer, String>>();
	private Map<Integer, String> answers = new HashMap<Integer, String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		SURVEY = getResources().getInteger(R.integer.survey);
		QUESTIONS = getResources().getInteger(R.integer.questions);
		getNextQuestion();
	}

	private void getNextQuestion() {
		int id = getResources().getIdentifier(String.format("question%d", questionNumber), "array", getPackageName());
		TypedArray data = getResources().obtainTypedArray(id);
		TextView question = (TextView) inflateItem(R.layout.label);
		question.setTextSize(24);
		question.setText(data.getString(1));
		LinearLayout layout = (LinearLayout) findViewById(R.id.screen);

		ScrollView scroll = (ScrollView) findViewById(R.id.screen_scroll);
		scroll.smoothScrollTo(0, 0);
		scroll.computeScroll();

		layout.removeAllViews();
		layout.addView(question);

		answers = new HashMap<Integer, String>();
		if (formAnswers.get(questionNumber) != null) {
			answers.putAll(formAnswers.get(questionNumber));
		}

		if (data.getInt(0, 0) == QuestionType.MULTIPLE.ordinal()) {
			addMultiple(data, layout, 2);
		} else if (data.getInt(0, 0) == QuestionType.UNIQUE.ordinal()) {
			addUnique(data, layout, 2);
		} else if (data.getInt(0, 0) == QuestionType.GRADE.ordinal()) {
			addGrade(data, layout, 3);
		} else if (data.getInt(0, 0) == QuestionType.OPEN.ordinal()) {
			addOpen(data, layout, 3);
		} else if (data.getInt(0, 0) == QuestionType.ORDER.ordinal()) {
			addOrder(data, layout, 2);
		}
		addHandleButtons(layout);

	}

	private void addHandleButtons(LinearLayout layout) {
		LinearLayout control = (LinearLayout) inflateItem(R.layout.control_buttons);
		Button backButton = (Button) control.getChildAt(0);
		Button nextButton = (Button) control.getChildAt(1);

		if (questionNumber > 0) {

			backButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					questionNumber--;
					getNextQuestion();
				}
			});
			backButton.setText(BACK_QUESTION);
			// layout.addView(backButton);
		} else {
			backButton.setVisibility(View.INVISIBLE);
		}

		if (questionNumber < QUESTIONS - 1) {
			nextButton.setText(NEXT_QUESTION);
			nextButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!answers.isEmpty()) {
						questionNumber++;
						getNextQuestion();
					}
				}
			});
		} else {
			nextButton.setText(END_FORM);
			nextButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!formAnswers.get(questionNumber).isEmpty()) {
						postAnswers();
					}
				}
			});
		}
		layout.addView(control);
		// layout.addView(nextButton);
	}

	protected boolean postAnswers() {
		DefaultHttpClient client = new DefaultHttpClient();

		try {
			HttpPost post = new HttpPost(URL);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("survey", SURVEY.toString()));

			for (Integer i = 0; i < QUESTIONS; i++) {
				String tmp;
				if (!formAnswers.containsKey(i)) {
					tmp = "";
				} else {
					tmp = formAnswers.get(i).toString();
					tmp = tmp.substring(1, tmp.length() - 1);
				}
				nameValuePairs.add(new BasicNameValuePair(i.toString(), tmp));
			}

			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				throw new Exception();
			}
			Toast msg = Toast.makeText(getApplicationContext(), "Pesquisa enviada com sucesso!", Toast.LENGTH_LONG);
			msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
			msg.show();

		} catch (Exception e) {
			Toast msg = Toast.makeText(this, "Envio falhou! Tente novamente.", Toast.LENGTH_LONG);
			msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
			msg.show();
			return false;
		}
		return true;
	}

	private void addOpen(TypedArray data, LinearLayout layout, int offset) {
		for (int i = offset; i < data.length(); i++) {
			final Integer q = i - offset;
			final EditText answer = (EditText) inflateItem(R.layout.answer_text);
			if (answers.containsKey(q)) {
				answer.setText(answers.get(q));
			}
			answer.setMaxLines(data.getInt(2, 1));
			answer.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					answers.put(q, answer.getText().toString());
					formAnswers.put(questionNumber, answers);
					return false;
				}
			});
			layout.addView(answer);
		}
	}

	private void addGrade(TypedArray data, LinearLayout layout, int offset) {
		for (int i = offset; i < data.length(); i++) {
			final Integer q = i - offset;
			SeekBar answer = (SeekBar) inflateItem(R.layout.answer_rating);
			answer.setMax(data.getInt(2, 5));
			final TextView label = (TextView) inflateItem(R.layout.label);
			if (answers.containsKey(q)) {
				answer.setProgress(Integer.parseInt(answers.get(q)));
				label.setText("" + Integer.parseInt(answers.get(q)));
			} else {
				label.setText("1");
			}
			TextView question = (TextView) inflateItem(R.layout.label);
			question.setText(data.getString(i));
			layout.addView(question);
			answer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					label.setText(((Integer) progress).toString());
					answers.put(q, ((Integer) progress).toString());
					formAnswers.put(questionNumber, answers);
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
			});
			layout.addView(answer);
			layout.addView(label);
		}
	}

	private void addMultiple(TypedArray data, LinearLayout layout, int offset) {
		for (int i = offset; i < data.length(); i++) {
			final Integer q = i - offset;
			CheckBox answer = (CheckBox) inflateItem(R.layout.answer_checkbox);
			if (answers.containsKey(q)) {
				answer.setChecked(true);
			}
			final String[] options = getOptions(data.getString(i));
			String text = options[options.length - 1];

			final EditText answerText = (EditText) inflateItem(R.layout.answer_text);
			answer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (options[0].equals("OPEN")) {
						answerText.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
						answerText.setText(null);
					}
					if (!isChecked) {
						answers.remove(q);
					} else {
						answers.put(q, q.toString());
					}
					formAnswers.put(questionNumber, answers);
				}
			});
			layout.addView(answer);
			answer.setText(text);
			if (options[0].equals("OPEN")) {
				if (!answers.containsKey(q)) {
					answerText.setVisibility(View.INVISIBLE);
				} else {
					answerText.setText(answers.get(q));
				}
				answerText.setOnKeyListener(new View.OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						answers.put(q, answerText.getText().toString());
						formAnswers.put(questionNumber, answers);
						return false;
					}
				});
				layout.addView(answerText);
			}
		}
	}

	private View inflateItem(int id) {
		return getLayoutInflater().inflate(id, null);
	}

	private String[] getOptions(String data) {
		return data.split(";");
	}

	private void addOrder(TypedArray data, LinearLayout layout, int offset) {
		for (int i = offset; i < data.length(); i++) {
			final Integer q = i - offset;
			LinearLayout group = (LinearLayout) inflateItem(R.layout.answer_order);
			final String[] options = getOptions(data.getString(i));
			String text = options[options.length - 1];

			TextView label = (TextView) group.getChildAt(1);
			label.setText(text);

			final EditText answer = (EditText) group.getChildAt(0);
			if (answers.containsKey(q)) {
				answer.setText(answers.get(q));
			}
			answer.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					answers.put(q, answer.getText().toString());
					formAnswers.put(questionNumber, answers);
					return false;
				}
			});

			layout.addView(group);
		}
	}

	private void addUnique(TypedArray data, LinearLayout layout, int offset) {
		RadioGroup group = (RadioGroup) inflateItem(R.layout.answer_radiogroup);
		final int question = questionNumber;
		for (int i = offset; i < data.length(); i++) {
			final Integer q = i - offset;
			RadioButton answer = (RadioButton) inflateItem(R.layout.answer_radiobutton);
			answer.setId(q);
			if (answers.containsKey(q)) {
				answer.setChecked(true);
			}
			final String[] options = getOptions(data.getString(i));
			Integer _skip = 0;
			final EditText answerText = (EditText) inflateItem(R.layout.answer_text);
			if (options.length > 1) {
				if (!options[0].equals("OPEN")) {
					_skip = Integer.parseInt(options[0]);
				}
			}
			final Integer skip = _skip;
			String text = options[options.length - 1];

			answer.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						questionNumber = question + skip;
					}
					if (options[0].equals("OPEN")) {
						answerText.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
					}
				}
			});
			answer.setText(text);
			group.addView(answer);
			answerText.setWidth(1000);
			if (options[0].equals("OPEN")) {
				if (!answers.containsKey(q)) {
					answerText.setVisibility(View.INVISIBLE);
				} else {
					answerText.setText(answers.get(q));
				}
				answerText.setOnKeyListener(new View.OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						answers.put(q, answerText.getText().toString());
						formAnswers.put(question, answers);
						return false;
					}
				});
				group.addView(answerText);
			}

			group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					answers.clear();
					answers.put(checkedId, ((Integer) checkedId).toString());
					formAnswers.put(question, answers);
				}
			});
		}
		layout.addView(group);
	}
}