package uk.co.pactsolutions.teslachecklist;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout list;
    private TextView progress;
    private final ArrayList<ItemRow> rows = new ArrayList<>();
    private android.content.SharedPreferences prefs;

    private static class CheckItem {
        String section;
        String text;
        CheckItem(String section, String text) { this.section = section; this.text = text; }
    }

    private class ItemRow {
        CheckItem item;
        RadioGroup statusGroup;
        EditText notes;
        ItemRow(CheckItem item, RadioGroup statusGroup, EditText notes) {
            this.item = item; this.statusGroup = statusGroup; this.notes = notes;
        }
    }

    private final CheckItem[] CHECKS = new CheckItem[] {
        new CheckItem("Before You Arrive", "Bring your driver's licence"),
        new CheckItem("Before You Arrive", "Tesla app downloaded and logged in"),
        new CheckItem("Before You Arrive", "VIN and delivery appointment confirmed"),
        new CheckItem("Before You Arrive", "Bring paperwork or trade-in documents if required"),
        new CheckItem("Before You Arrive", "Phone fully charged"),

        new CheckItem("Exterior Inspection", "No scratches, dents, or paint chips on body panels"),
        new CheckItem("Exterior Inspection", "Consistent panel gaps and alignment: doors, trunk, frunk, charge port"),
        new CheckItem("Exterior Inspection", "No smudges or uneven paint blending"),
        new CheckItem("Exterior Inspection", "Door handles flush and functional"),
        new CheckItem("Exterior Inspection", "No loose trim or rubber seals"),

        new CheckItem("Glass & Mirrors", "Windscreen and windows free from cracks, chips, or scratches"),
        new CheckItem("Glass & Mirrors", "Mirrors properly attached and functional"),
        new CheckItem("Glass & Mirrors", "Roof glass free from distortion or bubbling"),

        new CheckItem("Wheels & Tyres", "All rims scratch-free and undamaged"),
        new CheckItem("Wheels & Tyres", "Tyres match the expected spec and correct size"),
        new CheckItem("Wheels & Tyres", "Adequate tyre tread and proper inflation"),
        new CheckItem("Wheels & Tyres", "Check under the car for visible damage"),

        new CheckItem("Lights & Exterior Features", "Headlights, taillights, and indicators aligned and working"),
        new CheckItem("Lights & Exterior Features", "Charge port opens and closes properly"),
        new CheckItem("Lights & Exterior Features", "Door handles extend/retract correctly"),
        new CheckItem("Lights & Exterior Features", "Cameras clean and lens covers intact"),
        new CheckItem("Lights & Exterior Features", "Frunk opens smoothly and seals properly"),
        new CheckItem("Lights & Exterior Features", "Trunk opens/closes without resistance or misalignment"),
        new CheckItem("Lights & Exterior Features", "Frunk and trunk carpeting clean and attached"),
        new CheckItem("Lights & Exterior Features", "Mobile charging cable present if included"),
        new CheckItem("Lights & Exterior Features", "Emergency triangle / first aid kit if applicable"),

        new CheckItem("Interior Inspection", "No marks, stains, or creases on seats"),
        new CheckItem("Interior Inspection", "All seat controls functional"),
        new CheckItem("Interior Inspection", "Seatbelts retract smoothly and latch securely"),
        new CheckItem("Interior Inspection", "Dashboard, centre console, and door trims scratch-free"),
        new CheckItem("Interior Inspection", "No rattling sounds when doors close"),

        new CheckItem("Touchscreen & Controls", "Screen bright, responsive, and scratch-free"),
        new CheckItem("Touchscreen & Controls", "Buttons, scroll wheels, and steering controls work"),
        new CheckItem("Touchscreen & Controls", "Volume and climate controls respond properly"),
        new CheckItem("Touchscreen & Controls", "Check software version in Settings > Software"),

        new CheckItem("Cabin Tech & Features", "Test A/C and heater on all vents"),
        new CheckItem("Cabin Tech & Features", "Test heated seats where applicable"),
        new CheckItem("Cabin Tech & Features", "Bluetooth pairs with your phone"),
        new CheckItem("Cabin Tech & Features", "Audio system works properly"),
        new CheckItem("Cabin Tech & Features", "Test reversing camera and visualisation"),

        new CheckItem("Doors & Windows", "All doors open, close, and lock smoothly"),
        new CheckItem("Doors & Windows", "Windows roll up/down without noise"),
        new CheckItem("Doors & Windows", "Child locks functional if applicable"),

        new CheckItem("Mirrors & Wipers", "Mirrors auto-dim and adjust via controls"),
        new CheckItem("Mirrors & Wipers", "Rear-view mirror properly aligned"),
        new CheckItem("Mirrors & Wipers", "Wipers function with no smears or squeaks"),
        new CheckItem("Mirrors & Wipers", "Washer fluid sprays correctly"),

        new CheckItem("App + Key Access", "Tesla app connects and unlocks car"),
        new CheckItem("App + Key Access", "Test mobile key and/or key card"),
        new CheckItem("App + Key Access", "Add second phone/user if needed"),
        new CheckItem("App + Key Access", "Try remote lock/unlock and climate control"),

        new CheckItem("Charging Check", "Charge port door opens from app, screen, and touch"),
        new CheckItem("Charging Check", "Mobile charger present if included"),
        new CheckItem("Charging Check", "Test charger unlock button"),
        new CheckItem("Charging Check", "Plug in at delivery point if possible"),

        new CheckItem("Documentation", "Vehicle logbook / V5C submitted if UK"),
        new CheckItem("Documentation", "Confirm correct vehicle spec and VIN"),
        new CheckItem("Documentation", "Warranty, manual, and service info provided digitally"),
        new CheckItem("Documentation", "Insurance active from delivery date"),
        new CheckItem("Documentation", "Vehicle taxed and ready to drive if UK"),

        new CheckItem("Bonus Checks", "Number plates correct and securely fitted"),
        new CheckItem("Bonus Checks", "Test horn"),
        new CheckItem("Bonus Checks", "Included accessories present: floor mats, sunshade, tow hook, etc."),
        new CheckItem("Bonus Checks", "Take a test drive around the lot/block if possible"),

        new CheckItem("After Delivery", "Back up car profile/settings to Tesla Account"),
        new CheckItem("After Delivery", "Schedule first software update if needed"),
        new CheckItem("After Delivery", "Set up home/work charging in Navigation"),
        new CheckItem("After Delivery", "Set Sentry Mode preferences"),
        new CheckItem("After Delivery", "Review delivery photos")
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("tesla_checklist", MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 245, 245));

        TextView title = new TextView(this);
        title.setText("Tesla Delivery Checklist");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, 1);
        title.setPadding(24, 24, 24, 8);
        title.setBackgroundColor(Color.rgb(190, 0, 0));
        root.addView(title);

        progress = new TextView(this);
        progress.setTextColor(Color.WHITE);
        progress.setTextSize(15);
        progress.setPadding(24, 0, 24, 18);
        progress.setBackgroundColor(Color.rgb(190, 0, 0));
        root.addView(progress);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(12, 12, 12, 6);
        buttons.addView(makeButton("Share Report", v -> shareReport()), weightParams());
        buttons.addView(makeButton("Issues", v -> showIssues()), weightParams());
        buttons.addView(makeButton("Reset", v -> confirmReset()), weightParams());
        root.addView(buttons);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(12, 6, 12, 24);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        buildChecklist();
        updateProgress();
    }

    private Button makeButton(String text, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        return b;
    }

    private LinearLayout.LayoutParams weightParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(4, 0, 4, 0);
        return p;
    }

    private void buildChecklist() {
        String currentSection = "";
        for (int i = 0; i < CHECKS.length; i++) {
            CheckItem item = CHECKS[i];
            if (!item.section.equals(currentSection)) {
                currentSection = item.section;
                TextView header = new TextView(this);
                header.setText(currentSection);
                header.setTextSize(20);
                header.setTypeface(null, 1);
                header.setTextColor(Color.rgb(40, 40, 40));
                header.setPadding(4, 24, 4, 8);
                list.addView(header);
            }
            addItemRow(i, item);
        }
    }

    private void addItemRow(final int index, CheckItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        card.setPadding(18, 16, 18, 16);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, 0, 0, 12);
        list.addView(card, cp);

        TextView question = new TextView(this);
        question.setText(item.text);
        question.setTextSize(16);
        question.setTextColor(Color.rgb(30,30,30));
        card.addView(question);

        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.HORIZONTAL);
        String[] labels = {"Pass", "Issue", "N/A"};
        for (int j=0; j<labels.length; j++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(labels[j]);
            rb.setId(1000 + index * 10 + j);
            rg.addView(rb);
        }
        int savedStatus = prefs.getInt("status_" + index, -1);
        if (savedStatus >= 0) rg.check(1000 + index * 10 + savedStatus);
        card.addView(rg);

        EditText notes = new EditText(this);
        notes.setHint("Notes, if needed");
        notes.setSingleLine(false);
        notes.setMinLines(1);
        notes.setText(prefs.getString("notes_" + index, ""));
        card.addView(notes);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            int status = checkedId - (1000 + index * 10);
            prefs.edit().putInt("status_" + index, status).apply();
            updateProgress();
        });
        notes.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) saveNote(index, notes); });
        notes.setOnEditorActionListener((v, actionId, event) -> { saveNote(index, notes); return false; });
        rows.add(new ItemRow(item, rg, notes));
    }

    private void saveNote(int index, EditText notes) {
        prefs.edit().putString("notes_" + index, notes.getText().toString()).apply();
    }

    private void saveAllNotes() {
        android.content.SharedPreferences.Editor e = prefs.edit();
        for (int i=0; i<rows.size(); i++) e.putString("notes_" + i, rows.get(i).notes.getText().toString());
        e.apply();
    }

    private void updateProgress() {
        int done = 0, issues = 0;
        for (int i=0; i<CHECKS.length; i++) {
            int s = prefs.getInt("status_" + i, -1);
            if (s >= 0) done++;
            if (s == 1) issues++;
        }
        progress.setText(done + " / " + CHECKS.length + " checks complete" + (issues > 0 ? " • " + issues + " issue(s)" : ""));
    }

    private String buildReport() {
        saveAllNotes();
        StringBuilder sb = new StringBuilder();
        sb.append("Tesla Delivery Checklist Report\n");
        sb.append(new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.UK).format(new Date())).append("\n\n");
        String current = "";
        for (int i=0; i<CHECKS.length; i++) {
            CheckItem item = CHECKS[i];
            if (!item.section.equals(current)) {
                current = item.section;
                sb.append("\n").append(current).append("\n");
            }
            int s = prefs.getInt("status_" + i, -1);
            String status = s == 0 ? "PASS" : s == 1 ? "ISSUE" : s == 2 ? "N/A" : "NOT CHECKED";
            String note = prefs.getString("notes_" + i, "").trim();
            sb.append("- [").append(status).append("] ").append(item.text);
            if (!note.isEmpty()) sb.append(" — ").append(note);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void shareReport() {
        String report = buildReport();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Tesla Delivery Checklist Report");
        send.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(send, "Share checklist report"));
    }

    private void showIssues() {
        saveAllNotes();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i=0; i<CHECKS.length; i++) {
            if (prefs.getInt("status_" + i, -1) == 1) {
                count++;
                String note = prefs.getString("notes_" + i, "").trim();
                sb.append(count).append(". ").append(CHECKS[i].section).append(" - ").append(CHECKS[i].text);
                if (!note.isEmpty()) sb.append("\n   Note: ").append(note);
                sb.append("\n\n");
            }
        }
        if (count == 0) sb.append("No issues marked yet.");
        new AlertDialog.Builder(this).setTitle("Issues").setMessage(sb.toString()).setPositiveButton("OK", null).show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
            .setTitle("Reset checklist?")
            .setMessage("This clears all ticks and notes on this phone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Reset", (d, w) -> { prefs.edit().clear().apply(); recreate(); })
            .show();
    }
}
