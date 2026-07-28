package uk.co.pactsolutions.teslachecklist;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(10, 12, 18);
    private static final int SURFACE = Color.rgb(24, 28, 38);
    private static final int SURFACE_2 = Color.rgb(35, 40, 54);
    private static final int TESLA_RED = Color.rgb(220, 28, 46);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int MUTED = Color.rgb(174, 181, 194);
    private static final int BORDER = Color.rgb(64, 72, 90);
    private static final int REQUEST_PICK_ISSUE_PHOTO = 2001;
    private static final int REQUEST_TAKE_ISSUE_PHOTO = 2002;

    private LinearLayout list;
    private TextView progress;
    private ProgressBar progressBar;
    private Spinner sectionSpinner;
    private ArrayAdapter<String> sectionAdapter;
    private boolean updatingSectionSpinner = false;
    private final ArrayList<ItemRow> rows = new ArrayList<>();
    private final ArrayList<CheckItem> activeChecks = new ArrayList<>();
    private final ArrayList<String> sectionNames = new ArrayList<>();
    private final LinkedHashMap<String, LinearLayout> sectionContentViews = new LinkedHashMap<>();
    private String openSection;
    private int pendingIssuePhotoIndex = -1;
    private Uri pendingCameraUri;
    private ImageView pendingIssuePhotoPreview;
    private android.content.SharedPreferences prefs;
    private android.content.SharedPreferences selectionPrefs;

    private static class CheckItem {
        String section;
        String text;
        CheckItem(String section, String text) { this.section = section; this.text = text; }
    }

    private class ItemRow {
        int index;
        CheckItem item;
        RadioGroup statusGroup;
        EditText notes;
        ItemRow(int index, CheckItem item, RadioGroup statusGroup, EditText notes) {
            this.index = index; this.item = item; this.statusGroup = statusGroup; this.notes = notes;
        }
    }

    private final CheckItem[] CHECKS = new CheckItem[] {
        new CheckItem("Before You Arrive", "Bring your driver's licence"),
        new CheckItem("Before You Arrive", "Tesla app downloaded and logged in"),
        new CheckItem("Before You Arrive", "VIN and delivery appointment confirmed"),
        new CheckItem("Before You Arrive", "Bring paperwork or trade-in documents if required"),
        new CheckItem("Before You Arrive", "Phone fully charged"),

        new CheckItem("Exterior Checks Stage 1", "No scratches, dents, or paint chips on body panels"),
        new CheckItem("Exterior Checks Stage 1", "Consistent panel gaps and alignment: doors, trunk, frunk, charge port"),
        new CheckItem("Exterior Checks Stage 1", "No smudges or uneven paint blending"),
        new CheckItem("Exterior Checks Stage 1", "Door handles flush and functional"),
        new CheckItem("Exterior Checks Stage 1", "No loose trim or rubber seals"),

        new CheckItem("Exterior Checks Stage 1", "Windscreen and windows free from cracks, chips, or scratches"),
        new CheckItem("Exterior Checks Stage 1", "Mirrors properly attached and functional"),
        new CheckItem("Exterior Checks Stage 1", "Roof glass free from distortion, cracks, chips, or scratches"),

        new CheckItem("Exterior Checks Stage 1", "All rims scratch-free and undamaged"),
        new CheckItem("Exterior Checks Stage 1", "Tyres match the expected spec and correct size"),
        new CheckItem("Exterior Checks Stage 1", "Adequate tyre tread and proper inflation"),
        new CheckItem("Exterior Checks Stage 1", "Check under the car for visible damage"),

        new CheckItem("Exterior Checks Stage 2", "Headlights, taillights, and indicators aligned and working"),
        new CheckItem("Exterior Checks Stage 2", "Cameras clean and lens covers intact"),
        new CheckItem("Exterior Checks Stage 2", "Frunk opens smoothly and seals properly"),
        new CheckItem("Exterior Checks Stage 2", "Trunk opens/closes without resistance or misalignment"),
        new CheckItem("Exterior Checks Stage 2", "Frunk and trunk carpeting clean and attached"),
        new CheckItem("Exterior Checks Stage 2", "Emergency triangle / first aid kit if applicable"),

        new CheckItem("Interior & Cabin Tech", "No marks, stains, or creases on seats"),
        new CheckItem("Interior & Cabin Tech", "All seat controls functional"),
        new CheckItem("Interior & Cabin Tech", "Seatbelts retract smoothly and latch securely"),
        new CheckItem("Interior & Cabin Tech", "Dashboard, centre console, and door trims scratch-free"),
        new CheckItem("Interior & Cabin Tech", "No rattling sounds when doors close"),

        new CheckItem("Interior & Cabin Tech", "Screen bright, responsive, and scratch-free"),
        new CheckItem("Interior & Cabin Tech", "Buttons, scroll wheels, and steering controls work"),
        new CheckItem("Interior & Cabin Tech", "Volume and climate controls respond properly"),
        new CheckItem("Interior & Cabin Tech", "Check software version in Settings > Software"),

        new CheckItem("Interior & Cabin Tech", "Test A/C and heater on all vents"),
        new CheckItem("Interior & Cabin Tech", "Test heated seats where applicable"),
        new CheckItem("Interior & Cabin Tech", "Bluetooth pairs with your phone"),
        new CheckItem("Interior & Cabin Tech", "Audio system works properly"),
        new CheckItem("Interior & Cabin Tech", "Test reversing camera and visualisation"),

        new CheckItem("Interior & Cabin Tech", "All doors open, close, and lock smoothly"),
        new CheckItem("Interior & Cabin Tech", "Windows roll up/down without noise"),
        new CheckItem("Interior & Cabin Tech", "Child locks functional if applicable"),

        new CheckItem("Exterior Checks Stage 2", "Mirrors auto-dim and adjust via controls"),
        new CheckItem("Exterior Checks Stage 2", "Rear-view mirror properly aligned"),
        new CheckItem("Exterior Checks Stage 2", "Wipers function with no smears or squeaks"),
        new CheckItem("Exterior Checks Stage 2", "Washer fluid sprays correctly"),

        new CheckItem("Access, Charging & Documents", "Tesla app connects and unlocks car"),
        new CheckItem("Access, Charging & Documents", "Test mobile key and/or key card"),
        new CheckItem("Access, Charging & Documents", "Add second phone/user if needed"),
        new CheckItem("Access, Charging & Documents", "Try remote climate control"),

        new CheckItem("Access, Charging & Documents", "Charge port door opens from app, screen, and touch, then closes properly"),
        new CheckItem("Access, Charging & Documents", "Mobile charger present if included"),
        new CheckItem("Access, Charging & Documents", "Test charger unlock button"),
        new CheckItem("Access, Charging & Documents", "Plug in at delivery point if possible"),

        new CheckItem("Access, Charging & Documents", "Vehicle logbook / V5C submitted if UK"),
        new CheckItem("Access, Charging & Documents", "Confirm correct vehicle spec and VIN"),
        new CheckItem("Access, Charging & Documents", "Warranty, manual, and service info provided digitally"),
        new CheckItem("Access, Charging & Documents", "Insurance active from delivery date"),
        new CheckItem("Access, Charging & Documents", "Vehicle taxed and ready to drive if UK"),

        new CheckItem("Final Checks", "Number plates correct and securely fitted"),
        new CheckItem("Final Checks", "Test horn"),
        new CheckItem("Final Checks", "Included accessories present: floor mats, sunshade, tow hook, etc."),
        new CheckItem("Final Checks", "Take a test drive around the lot/block if possible"),

        new CheckItem("Final Checks", "Back up car profile/settings to Tesla Account"),
        new CheckItem("Final Checks", "Schedule first software update if needed"),
        new CheckItem("Final Checks", "Set up home/work charging in Navigation"),
        new CheckItem("Final Checks", "Set Sentry Mode preferences"),
        new CheckItem("Final Checks", "Take a nice exterior photo of the car"),
        new CheckItem("Final Checks", "Take a nice interior photo of the cabin"),
        new CheckItem("Final Checks", "Review delivery photos")
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        selectionPrefs = getSharedPreferences("tesla_checklist", MODE_PRIVATE);
        migrateLegacyModel3Checklist();
        migrateRemovedDoorHandleCheck();
        migrateDuplicateChecks();
        setChecklistPreferences(selectedModel());
        showLandingPage();
    }

    private String selectedModel() {
        return selectionPrefs.getString("selected_model", "Model 3");
    }

    private void setChecklistPreferences(String model) {
        String suffix = model.toLowerCase(Locale.UK).replace(" ", "_");
        prefs = getSharedPreferences("tesla_checklist_" + suffix, MODE_PRIVATE);
    }

    private void selectModel(String model) {
        selectionPrefs.edit().putString("selected_model", model).apply();
        setChecklistPreferences(model);
        showChecklistPage();
    }

    private void migrateLegacyModel3Checklist() {
        if (selectionPrefs.getBoolean("model_storage_migrated", false)) return;

        android.content.SharedPreferences model3Prefs =
            getSharedPreferences("tesla_checklist_model_3", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = model3Prefs.edit();
        for (Map.Entry<String, ?> entry : selectionPrefs.getAll().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if ("selected_model".equals(key) || "model_storage_migrated".equals(key)) continue;
            if (value instanceof String) editor.putString(key, (String) value);
            else if (value instanceof Integer) editor.putInt(key, (Integer) value);
            else if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
            else if (value instanceof Long) editor.putLong(key, (Long) value);
            else if (value instanceof Float) editor.putFloat(key, (Float) value);
        }
        editor.apply();
        selectionPrefs.edit().putBoolean("model_storage_migrated", true).apply();
    }

    private void migrateRemovedDoorHandleCheck() {
        if (selectionPrefs.getBoolean("door_handle_check_removed_migrated", false)) return;

        migrateChecklistAfterRemovedItem(getSharedPreferences("tesla_checklist_model_3", MODE_PRIVATE), 19);
        migrateChecklistAfterRemovedItem(getSharedPreferences("tesla_checklist_model_y", MODE_PRIVATE), 19);
        selectionPrefs.edit().putBoolean("door_handle_check_removed_migrated", true).apply();
    }

    private void migrateChecklistAfterRemovedItem(
        android.content.SharedPreferences checklistPrefs,
        int removedIndex
    ) {
        android.content.SharedPreferences.Editor editor = checklistPrefs.edit();
        String[] prefixes = {"status_", "notes_", "photo_"};
        final int checklistCountAfterRemoval = 70;
        for (int index = removedIndex; index < checklistCountAfterRemoval; index++) {
            for (String prefix : prefixes) {
                String destination = prefix + index;
                String source = prefix + (index + 1);
                if (!checklistPrefs.contains(source)) {
                    editor.remove(destination);
                } else if ("status_".equals(prefix)) {
                    editor.putInt(destination, checklistPrefs.getInt(source, -1));
                } else {
                    editor.putString(destination, checklistPrefs.getString(source, ""));
                }
            }
        }
        for (String prefix : prefixes) editor.remove(prefix + checklistCountAfterRemoval);
        editor.apply();
    }

    private void migrateDuplicateChecks() {
        if (selectionPrefs.getBoolean("duplicate_checks_removed_migrated", false)) return;

        migrateChecklistAfterDuplicateRemoval(getSharedPreferences("tesla_checklist_model_3", MODE_PRIVATE));
        migrateChecklistAfterDuplicateRemoval(getSharedPreferences("tesla_checklist_model_y", MODE_PRIVATE));
        selectionPrefs.edit().putBoolean("duplicate_checks_removed_migrated", true).apply();
    }

    private void migrateChecklistAfterDuplicateRemoval(android.content.SharedPreferences checklistPrefs) {
        android.content.SharedPreferences.Editor editor = checklistPrefs.edit();
        String[] prefixes = {"status_", "notes_", "photo_"};
        for (int destination = 0; destination < CHECKS.length; destination++) {
            int source = destination < 18 ? destination : destination < 22 ? destination + 1 : destination + 2;
            if (source == destination) continue;
            for (String prefix : prefixes) {
                String destinationKey = prefix + destination;
                String sourceKey = prefix + source;
                if (!checklistPrefs.contains(sourceKey)) {
                    editor.remove(destinationKey);
                } else if ("status_".equals(prefix)) {
                    editor.putInt(destinationKey, checklistPrefs.getInt(sourceKey, -1));
                } else {
                    editor.putString(destinationKey, checklistPrefs.getString(sourceKey, ""));
                }
            }
        }
        for (int oldIndex = CHECKS.length; oldIndex < 70; oldIndex++) {
            for (String prefix : prefixes) editor.remove(prefix + oldIndex);
        }
        editor.apply();
    }

    private void reloadActiveChecks() {
        activeChecks.clear();
        Collections.addAll(activeChecks, CHECKS);
        int customCount = prefs.getInt("custom_count", 0);
        for (int i = 0; i < customCount; i++) {
            String customText = prefs.getString("custom_text_" + i, "").trim();
            if (!customText.isEmpty()) activeChecks.add(new CheckItem("Custom Checks", customText));
        }
    }

    private void showLandingPage() {
        rows.clear();
        progress = null;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(42), dp(22), dp(34));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView badge = text("DELIVERY CHECKLIST", 13, TESLA_RED, true);
        badge.setLetterSpacing(0.12f);
        badge.setGravity(Gravity.CENTER);
        root.addView(badge);

        TextView title = text("TesSure", 32, TEXT, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(10), 0, dp(8));
        root.addView(title);

        TextView subtitle = text("Choose your car, then work through the delivery checks before you drive away.", 16, MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setLineSpacing(dp(2), 1.0f);
        root.addView(subtitle);

        Space topSpace = new Space(this);
        root.addView(topSpace, new LinearLayout.LayoutParams(1, dp(28)));

        ViewFlipper modelCarousel = new ViewFlipper(this);
        TextView carouselIndicator = text("", 14, MUTED, true);
        carouselIndicator.setGravity(Gravity.CENTER);

        addModelCard(modelCarousel, carouselIndicator, "Model 3", "model_3", 0);
        addModelCard(modelCarousel, carouselIndicator, "Model Y", "model_y", 1);
        int initialModel = "Model Y".equals(selectedModel()) ? 1 : 0;
        modelCarousel.setDisplayedChild(initialModel);
        updateCarouselIndicator(carouselIndicator, initialModel);

        LinearLayout.LayoutParams carouselParams = new LinearLayout.LayoutParams(-1, -2);
        carouselParams.setMargins(0, 0, 0, dp(10));
        root.addView(modelCarousel, carouselParams);

        carouselIndicator.setPadding(0, 0, 0, dp(8));
        root.addView(carouselIndicator);

        setContentView(scroll);
    }

    private void addModelCard(
        ViewFlipper carousel,
        TextView indicator,
        String modelName,
        String drawableName,
        int modelIndex
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(22), dp(22), dp(22), dp(22));
        card.setBackground(rounded(SURFACE, dp(22), BORDER, 1));
        carousel.addView(card, new ViewGroup.LayoutParams(-1, -2));

        ImageView car = new ImageView(this);
        car.setImageResource(getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        car.setAdjustViewBounds(true);
        car.setScaleType(ImageView.ScaleType.FIT_CENTER);
        car.setContentDescription(modelName + ". Swipe left or right to choose another model.");
        final float[] touchStartX = new float[1];
        final float[] touchStartY = new float[1];
        final boolean[] swipeHandled = new boolean[1];
        car.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchStartX[0] = event.getX();
                touchStartY[0] = event.getY();
                swipeHandled[0] = false;
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE && !swipeHandled[0]) {
                float horizontal = event.getX() - touchStartX[0];
                float vertical = event.getY() - touchStartY[0];
                if (Math.abs(horizontal) >= dp(24) && Math.abs(horizontal) > Math.abs(vertical)) {
                    int target = horizontal < 0 ? modelIndex + 1 : modelIndex - 1;
                    showCarouselModel(carousel, indicator, target);
                    swipeHandled[0] = true;
                }
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                if (!swipeHandled[0]) view.performClick();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            return true;
        });
        card.addView(car, new LinearLayout.LayoutParams(-1, dp(170)));

        TextView model = text(modelName, 26, TEXT, true);
        model.setGravity(Gravity.CENTER);
        model.setPadding(0, dp(12), 0, dp(2));
        card.addView(model);

        TextView detail = text("Delivery-day inspection", 15, MUTED, false);
        detail.setGravity(Gravity.CENTER);
        card.addView(detail);

        android.content.SharedPreferences modelPrefs =
            getSharedPreferences("tesla_checklist_" + modelName.toLowerCase(Locale.UK).replace(" ", "_"), MODE_PRIVATE);
        boolean hasProgress = false;
        int modelCheckCount = CHECKS.length + modelPrefs.getInt("custom_count", 0);
        for (int i = 0; i < modelCheckCount && !hasProgress; i++) {
            hasProgress = modelPrefs.contains("status_" + i)
                || modelPrefs.contains("notes_" + i)
                || modelPrefs.contains("photo_" + i);
        }
        Button start = primaryButton((hasProgress ? "Continue " : "Start ") + modelName + " Checklist");
        start.setOnClickListener(v -> selectModel(modelName));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, dp(54));
        buttonParams.setMargins(0, dp(18), 0, 0);
        card.addView(start, buttonParams);
    }

    private void showCarouselModel(ViewFlipper carousel, TextView indicator, int target) {
        if (target < 0 || target >= carousel.getChildCount() || target == carousel.getDisplayedChild()) return;

        boolean movingLeft = target > carousel.getDisplayedChild();
        float incomingStart = movingLeft ? 1f : -1f;
        float outgoingEnd = movingLeft ? -1f : 1f;
        android.view.animation.TranslateAnimation inAnimation = new android.view.animation.TranslateAnimation(
            android.view.animation.Animation.RELATIVE_TO_SELF, incomingStart,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0
        );
        android.view.animation.TranslateAnimation outAnimation = new android.view.animation.TranslateAnimation(
            android.view.animation.Animation.RELATIVE_TO_SELF, 0,
            android.view.animation.Animation.RELATIVE_TO_SELF, outgoingEnd,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0
        );
        inAnimation.setDuration(220);
        outAnimation.setDuration(220);
        carousel.setInAnimation(inAnimation);
        carousel.setOutAnimation(outAnimation);
        carousel.setDisplayedChild(target);
        updateCarouselIndicator(indicator, target);
    }

    private void updateCarouselIndicator(TextView indicator, int modelIndex) {
        indicator.setText((modelIndex == 0 ? "●  ○" : "○  ●") + "   Swipe to select model");
        indicator.setContentDescription((modelIndex + 1) + " of 2. Swipe to select model.");
    }

    private void showChecklistPage() {
        rows.clear();
        sectionSpinner = null;
        sectionAdapter = null;
        reloadActiveChecks();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(42), dp(18), dp(14));
        header.setBackgroundColor(Color.rgb(14, 17, 25));
        root.addView(header);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(topRow, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        topRow.addView(titleBlock, new LinearLayout.LayoutParams(0, -2, 1));

        TextView eyebrow = text(selectedModel().toUpperCase(Locale.UK), 12, TESLA_RED, true);
        eyebrow.setLetterSpacing(0.12f);
        titleBlock.addView(eyebrow);

        TextView title = text("Delivery Checklist", 25, TEXT, true);
        title.setPadding(0, dp(4), 0, dp(2));
        titleBlock.addView(title);

        Button hamburger = secondaryButton("☰");
        hamburger.setTextSize(24);
        hamburger.setOnClickListener(v -> showChecklistMenu(v));
        LinearLayout.LayoutParams hamburgerParams = new LinearLayout.LayoutParams(dp(54), dp(48));
        topRow.addView(hamburger, hamburgerParams);

        progress = text("", 14, MUTED, false);
        progress.setPadding(0, dp(6), 0, 0);
        header.addView(progress);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(activeChecks.size());
        progressBar.setProgress(0);
        LinearLayout.LayoutParams progressBarParams = new LinearLayout.LayoutParams(-1, dp(8));
        progressBarParams.setMargins(0, dp(8), 0, 0);
        header.addView(progressBar, progressBarParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setClipToPadding(false);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(6), dp(12), dp(92));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        buildChecklist();
        updateProgress();
    }

    private void showChecklistMenu(View anchor) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(14), dp(12), dp(14), dp(12));
        menu.setBackground(rounded(Color.rgb(18, 22, 32), dp(18), BORDER, 1));

        TextView title = text("Checklist menu", 13, TESLA_RED, true);
        title.setLetterSpacing(0.08f);
        title.setPadding(dp(6), 0, dp(6), dp(8));
        menu.addView(title);

        final PopupWindow[] popup = new PopupWindow[1];
        addMenuItem(menu, "Choose Section", "Jump to another checklist section", true, () -> {
            popup[0].dismiss();
            showSectionPicker();
        });
        addMenuItem(menu, "View Issues", "Review issues with Tesla staff", false, () -> {
            popup[0].dismiss();
            showIssues();
        });
        addMenuItem(menu, "Share Report", "Send the full checklist report", false, () -> {
            popup[0].dismiss();
            shareReport();
        });
        addMenuItem(menu, "Cars", "Return to car selection", false, () -> {
            popup[0].dismiss();
            showLandingPage();
        });
        addMenuItem(menu, "Reset", "Clear checks and notes", false, () -> {
            popup[0].dismiss();
            confirmReset();
        });

        popup[0] = new PopupWindow(menu, dp(292), WindowManager.LayoutParams.WRAP_CONTENT, true);
        popup[0].setOutsideTouchable(true);
        popup[0].setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popup[0].setElevation(dp(10));
        popup[0].setAnimationStyle(getResources().getIdentifier("ChecklistPopupAnimation", "style", getPackageName()));
        popup[0].showAsDropDown(anchor, -dp(238), dp(8));
    }

    private void addMenuItem(LinearLayout menu, String label, String subtitle, boolean accent, Runnable action) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(12), dp(10), dp(12), dp(10));
        item.setClickable(true);
        item.setBackground(rounded(accent ? Color.rgb(43, 33, 42) : SURFACE_2, dp(12), accent ? TESLA_RED : BORDER, 1));
        item.setOnClickListener(v -> action.run());

        TextView labelView = text(label, 16, accent ? Color.WHITE : TEXT, true);
        item.addView(labelView);

        TextView subtitleView = text(subtitle, 12, MUTED, false);
        subtitleView.setPadding(0, dp(2), 0, 0);
        item.addView(subtitleView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(8));
        menu.addView(item, params);
    }

    private void showSectionPicker() {
        if (sectionNames.isEmpty()) return;
        CharSequence[] labels = new CharSequence[sectionNames.size()];
        for (int i = 0; i < sectionNames.size(); i++) labels[i] = sectionDropdownLabel(sectionNames.get(i));
        int selected = Math.max(0, sectionNames.indexOf(openSection));
        new AlertDialog.Builder(this)
            .setTitle("Choose section")
            .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                setOpenSection(sectionNames.get(which));
                dialog.dismiss();
            })
            .show();
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private Button primaryButton(String text) {
        Button b = baseButton(text);
        b.setTextColor(Color.WHITE);
        b.setBackground(rounded(TESLA_RED, dp(14), TESLA_RED, 0));
        return b;
    }

    private Button secondaryButton(String text) {
        Button b = baseButton(text);
        b.setTextColor(TEXT);
        b.setBackground(rounded(SURFACE_2, dp(14), BORDER, 1));
        return b;
    }

    private Button makeSmallButton(String text, View.OnClickListener l) {
        Button b = secondaryButton(text);
        b.setTextSize(13);
        b.setPadding(dp(4), 0, dp(4), 0);
        b.setOnClickListener(l);
        return b;
    }

    private Button baseButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setMinHeight(dp(48));
        b.setPadding(dp(10), 0, dp(10), 0);
        return b;
    }

    private LinearLayout.LayoutParams fullWidthButtonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54));
        p.setMargins(0, dp(8), 0, 0);
        return p;
    }

    private LinearLayout.LayoutParams weightParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(46), 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        return p;
    }

    private GradientDrawable rounded(int color, int radius, int strokeColor, int strokeWidthDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        if (strokeWidthDp > 0) d.setStroke(dp(strokeWidthDp), strokeColor);
        return d;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void buildChecklist() {
        sectionNames.clear();
        sectionContentViews.clear();
        openSection = null;

        for (int i = 0; i < activeChecks.size(); i++) {
            CheckItem item = activeChecks.get(i);
            LinearLayout currentContent = sectionContentViews.get(item.section);
            if (currentContent == null) {
                sectionNames.add(item.section);

                currentContent = new LinearLayout(this);
                currentContent.setOrientation(LinearLayout.VERTICAL);
                currentContent.setPadding(0, dp(8), 0, 0);
                list.addView(currentContent);

                TextView sectionTitle = text(item.section, 22, TEXT, true);
                sectionTitle.setPadding(dp(4), dp(4), dp(4), dp(8));
                currentContent.addView(sectionTitle);

                TextView sectionHint = text("Complete each item below. If you select Issue, an issue note field will appear.", 14, MUTED, false);
                sectionHint.setPadding(dp(4), 0, dp(4), dp(12));
                currentContent.addView(sectionHint);

                sectionContentViews.put(item.section, currentContent);
            }
            addItemRow(i, item, currentContent);
        }

        LinearLayout customContent = sectionContentViews.get("Custom Checks");
        if (customContent == null) {
            sectionNames.add("Custom Checks");
            customContent = new LinearLayout(this);
            customContent.setOrientation(LinearLayout.VERTICAL);
            customContent.setPadding(0, dp(8), 0, 0);
            list.addView(customContent);

            TextView sectionTitle = text("Custom Checks", 22, TEXT, true);
            sectionTitle.setPadding(dp(4), dp(4), dp(4), dp(8));
            customContent.addView(sectionTitle);

            TextView sectionHint = text("Add anything you want to inspect that is not covered elsewhere.", 14, MUTED, false);
            sectionHint.setPadding(dp(4), 0, dp(4), dp(12));
            customContent.addView(sectionHint);
            sectionContentViews.put("Custom Checks", customContent);
        }

        Button addCustomCheck = primaryButton("+ Add Custom Check");
        addCustomCheck.setOnClickListener(v -> showAddCustomCheckDialog());
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(-1, dp(52));
        addParams.setMargins(0, dp(4), 0, dp(12));
        customContent.addView(addCustomCheck, addParams);

        setupSectionDropdown();
        setOpenSection(sectionNames.isEmpty() ? null : sectionNames.get(0));
    }

    private void setupSectionDropdown() {
        if (sectionSpinner == null) return;
        sectionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sectionNames) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                styleSectionSpinnerText(view, position, false);
                return view;
            }

            @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                styleSectionSpinnerText(view, position, true);
                return view;
            }
        };
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(sectionAdapter);
        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!updatingSectionSpinner && position >= 0 && position < sectionNames.size()) {
                    setOpenSection(sectionNames.get(position));
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void showAddCustomCheckDialog() {
        final EditText input = new EditText(this);
        input.setHint("What would you like to check?");
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setTextColor(TEXT);
        input.setHintTextColor(MUTED);
        input.setPadding(dp(14), dp(10), dp(14), dp(10));

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Add Custom Check")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add", null)
            .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String checkText = input.getText().toString().trim();
            if (checkText.isEmpty()) {
                input.setError("Enter a check");
                return;
            }
            int customIndex = prefs.getInt("custom_count", 0);
            prefs.edit()
                .putString("custom_text_" + customIndex, checkText)
                .putInt("custom_count", customIndex + 1)
                .apply();
            dialog.dismiss();
            showChecklistPage();
            setOpenSection("Custom Checks");
        }));
        dialog.getWindow();
        dialog.show();
    }

    private void confirmRemoveCustomCheck(int customIndex, String checkText) {
        new AlertDialog.Builder(this)
            .setTitle("Remove custom check?")
            .setMessage(checkText)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Remove", (dialog, which) -> removeCustomCheck(customIndex))
            .show();
    }

    private void removeCustomCheck(int customIndex) {
        int customCount = prefs.getInt("custom_count", 0);
        if (customIndex < 0 || customIndex >= customCount) return;

        android.content.SharedPreferences.Editor editor = prefs.edit();
        for (int i = customIndex; i < customCount - 1; i++) {
            editor.putString("custom_text_" + i, prefs.getString("custom_text_" + (i + 1), ""));
            copyCustomValue(editor, "status_", CHECKS.length + i + 1, CHECKS.length + i);
            copyCustomValue(editor, "notes_", CHECKS.length + i + 1, CHECKS.length + i);
            copyCustomValue(editor, "photo_", CHECKS.length + i + 1, CHECKS.length + i);
        }
        int lastIndex = customCount - 1;
        editor.remove("custom_text_" + lastIndex);
        editor.remove("status_" + (CHECKS.length + lastIndex));
        editor.remove("notes_" + (CHECKS.length + lastIndex));
        editor.remove("photo_" + (CHECKS.length + lastIndex));
        editor.putInt("custom_count", lastIndex);
        editor.apply();
        showChecklistPage();
        setOpenSection("Custom Checks");
    }

    private void copyCustomValue(
        android.content.SharedPreferences.Editor editor,
        String prefix,
        int sourceIndex,
        int destinationIndex
    ) {
        String source = prefix + sourceIndex;
        String destination = prefix + destinationIndex;
        if (!prefs.contains(source)) {
            editor.remove(destination);
        } else if ("status_".equals(prefix)) {
            editor.putInt(destination, prefs.getInt(source, -1));
        } else {
            editor.putString(destination, prefs.getString(source, ""));
        }
    }

    private void styleSectionSpinnerText(TextView view, int position, boolean dropdown) {
        String section = position >= 0 && position < sectionNames.size() ? sectionNames.get(position) : "";
        view.setText(sectionDropdownLabel(section));
        view.setTextSize(16);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(TEXT);
        view.setPadding(dp(dropdown ? 16 : 12), dropdown ? dp(14) : 0, dp(12), dropdown ? dp(14) : 0);
        if (dropdown) view.setBackgroundColor(SURFACE_2);
    }

    private CharSequence sectionDropdownLabel(String section) {
        int issues = sectionIssueCount(section);
        boolean complete = sectionComplete(section);
        if (issues > 0) {
            String label = "● " + issues + "  " + section;
            SpannableString span = new SpannableString(label);
            span.setSpan(new ForegroundColorSpan(TESLA_RED), 0, Math.min(label.length(), 3 + String.valueOf(issues).length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return span;
        }
        if (complete) {
            String label = "✓  " + section;
            SpannableString span = new SpannableString(label);
            span.setSpan(new ForegroundColorSpan(Color.rgb(116, 242, 160)), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return span;
        }
        return section;
    }

    private void refreshSectionDropdown() {
        if (sectionAdapter != null) sectionAdapter.notifyDataSetChanged();
    }

    private void addItemRow(final int index, CheckItem item, LinearLayout parent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(rounded(SURFACE, dp(16), BORDER, 1));
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, 0, 0, dp(12));
        parent.addView(card, cp);

        TextView question = text(item.text, 16, TEXT, false);
        card.addView(question);

        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.HORIZONTAL);
        rg.setPadding(0, dp(6), 0, 0);
        String[] labels = {"Pass", "Issue", "N/A"};
        for (int j=0; j<labels.length; j++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(labels[j]);
            rb.setTextColor(MUTED);
            rb.setTextSize(14);
            rb.setId(1000 + index * 10 + j);
            rg.addView(rb);
        }
        int savedStatus = prefs.getInt("status_" + index, -1);
        if (savedStatus >= 0) rg.check(1000 + index * 10 + savedStatus);
        card.addView(rg);

        EditText notes = new EditText(this);
        notes.setText(prefs.getString("notes_" + index, ""));
        notes.setVisibility(View.GONE);
        card.addView(notes, new LinearLayout.LayoutParams(1, 1));

        Button editIssue = secondaryButton("Edit issue details");
        editIssue.setTextSize(14);
        editIssue.setVisibility(savedStatus == 1 ? View.VISIBLE : View.GONE);
        editIssue.setOnClickListener(v -> showIssueNoteDialog(index, item, notes));
        LinearLayout.LayoutParams editIssueParams = new LinearLayout.LayoutParams(-1, dp(44));
        editIssueParams.setMargins(0, dp(8), 0, 0);
        card.addView(editIssue, editIssueParams);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            int status = checkedId - (1000 + index * 10);
            prefs.edit().putInt("status_" + index, status).apply();
            editIssue.setVisibility(status == 1 ? View.VISIBLE : View.GONE);
            updateProgress();
            refreshSectionDropdown();
            if (status == 1) {
                showIssueNoteDialog(index, item, notes);
            } else {
                openNextSectionIfComplete(item.section);
            }
        });
        if (index >= CHECKS.length) {
            Button remove = secondaryButton("Remove custom check");
            remove.setTextSize(14);
            remove.setOnClickListener(v -> confirmRemoveCustomCheck(index - CHECKS.length, item.text));
            LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(-1, dp(44));
            removeParams.setMargins(0, dp(8), 0, 0);
            card.addView(remove, removeParams);
        }
        rows.add(new ItemRow(index, item, rg, notes));
    }

    private void setOpenSection(String section) {
        if (section == null && !sectionContentViews.isEmpty()) section = sectionContentViews.keySet().iterator().next();
        openSection = section;
        for (Map.Entry<String, LinearLayout> entry : sectionContentViews.entrySet()) {
            entry.getValue().setVisibility(entry.getKey().equals(openSection) ? View.VISIBLE : View.GONE);
        }
        if (sectionSpinner != null && openSection != null) {
            int index = sectionNames.indexOf(openSection);
            if (index >= 0 && sectionSpinner.getSelectedItemPosition() != index) {
                updatingSectionSpinner = true;
                sectionSpinner.setSelection(index);
                updatingSectionSpinner = false;
            }
        }
        updateProgress();
        refreshSectionDropdown();
    }

    private int sectionIssueCount(String section) {
        int issues = 0;
        for (int i = 0; i < activeChecks.size(); i++) {
            if (activeChecks.get(i).section.equals(section) && prefs.getInt("status_" + i, -1) == 1) issues++;
        }
        return issues;
    }

    private String sectionProgress(String section) {
        int done = 0;
        int total = 0;
        int issues = 0;
        for (int i = 0; i < activeChecks.size(); i++) {
            if (activeChecks.get(i).section.equals(section)) {
                total++;
                int status = prefs.getInt("status_" + i, -1);
                if (status >= 0) done++;
                if (status == 1) issues++;
            }
        }
        return done + " / " + total + " complete" + (issues > 0 ? " • " + issues + " issue(s)" : "");
    }

    private boolean sectionComplete(String section) {
        boolean hasItems = false;
        for (int i = 0; i < activeChecks.size(); i++) {
            if (activeChecks.get(i).section.equals(section)) {
                hasItems = true;
                if (prefs.getInt("status_" + i, -1) < 0) return false;
            }
        }
        return hasItems;
    }

    private String firstIncompleteSection() {
        for (String section : sectionContentViews.keySet()) {
            if (!sectionComplete(section)) return section;
        }
        return sectionContentViews.isEmpty() ? null : sectionContentViews.keySet().iterator().next();
    }

    private void openNextSectionIfComplete(String section) {
        if (!sectionComplete(section)) return;
        boolean next = false;
        for (String candidate : sectionContentViews.keySet()) {
            if (next) {
                setOpenSection(candidate);
                Toast.makeText(this, "Next section: " + candidate, Toast.LENGTH_SHORT).show();
                return;
            }
            if (candidate.equals(section)) next = true;
        }
        setOpenSection(section);
    }

    private void showIssueNoteDialog(int index, CheckItem item, EditText storedNotes) {
        final EditText input = new EditText(this);
        input.setHint("Type the issue found...");
        input.setSingleLine(false);
        input.setMinLines(5);
        input.setTextColor(TEXT);
        input.setHintTextColor(Color.rgb(120, 128, 145));
        input.setText(storedNotes.getText().toString());
        input.setSelection(input.getText().length());
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(rounded(SURFACE_2, dp(12), BORDER, 1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(10), dp(18), 0);

        TextView step = text("Step 1 of 2 — Describe the issue", 15, TESLA_RED, true);
        step.setPadding(0, 0, 0, dp(8));
        content.addView(step);

        TextView itemText = text(item.text, 15, MUTED, false);
        itemText.setPadding(0, 0, 0, dp(12));
        content.addView(itemText);
        content.addView(input, new LinearLayout.LayoutParams(-1, -2));

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Issue details")
            .setView(content)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Next: Add photo", null)
            .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setTextColor(TESLA_RED);
            positive.setOnClickListener(v -> {
                storedNotes.setText(input.getText().toString());
                saveNote(index, storedNotes);
                dialog.dismiss();
                showIssuePhotoDialog(index, item, storedNotes);
            });
            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negative.setTextColor(MUTED);

            input.requestFocus();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            WindowManager.LayoutParams params = window.getAttributes();
            params.y = dp(24);
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(rounded(SURFACE, dp(18), BORDER, 1));
        }
    }

    private void showIssuePhotoDialog(int index, CheckItem item, EditText storedNotes) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(10), dp(18), 0);

        TextView step = text("Step 2 of 2 — Add a photo", 15, TESLA_RED, true);
        step.setPadding(0, 0, 0, dp(8));
        content.addView(step);

        TextView helper = text("Add a photo from your gallery or take one now. You can skip this if a photo is not needed.", 15, MUTED, false);
        helper.setPadding(0, 0, 0, dp(12));
        content.addView(helper);

        ImageView preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackground(rounded(SURFACE_2, dp(12), BORDER, 1));
        content.addView(preview, new LinearLayout.LayoutParams(-1, dp(190)));
        updateIssuePhotoPreview(preview, prefs.getString("photo_" + index, ""));

        LinearLayout photoButtons = new LinearLayout(this);
        photoButtons.setOrientation(LinearLayout.HORIZONTAL);
        photoButtons.setPadding(0, dp(10), 0, 0);
        Button gallery = secondaryButton("Gallery");
        Button camera = secondaryButton("Camera");
        gallery.setOnClickListener(v -> pickIssuePhoto(index, preview));
        camera.setOnClickListener(v -> takeIssuePhoto(index, preview));
        photoButtons.addView(gallery, weightParams());
        photoButtons.addView(camera, weightParams());
        content.addView(photoButtons);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Issue photo")
            .setView(content)
            .setNegativeButton("Back", null)
            .setNeutralButton("Skip", null)
            .setPositiveButton("Done", null)
            .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setTextColor(TESLA_RED);
            positive.setOnClickListener(v -> {
                refreshSectionDropdown();
                dialog.dismiss();
                openNextSectionIfComplete(item.section);
            });

            Button neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            neutral.setTextColor(MUTED);
            neutral.setOnClickListener(v -> {
                refreshSectionDropdown();
                dialog.dismiss();
                openNextSectionIfComplete(item.section);
            });

            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negative.setTextColor(MUTED);
            negative.setOnClickListener(v -> {
                dialog.dismiss();
                showIssueNoteDialog(index, item, storedNotes);
            });
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            WindowManager.LayoutParams params = window.getAttributes();
            params.y = dp(24);
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(rounded(SURFACE, dp(18), BORDER, 1));
        }
    }

    private void pickIssuePhoto(int index, ImageView preview) {
        pendingIssuePhotoIndex = index;
        pendingIssuePhotoPreview = preview;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_ISSUE_PHOTO);
    }

    private void takeIssuePhoto(int index, ImageView preview) {
        pendingIssuePhotoIndex = index;
        pendingIssuePhotoPreview = preview;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "tesla_issue_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        pendingCameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (pendingCameraUri == null) {
            Toast.makeText(this, "Could not create photo file", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingCameraUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_TAKE_ISSUE_PHOTO);
    }

    private void updateIssuePhotoPreview(ImageView preview, String uriText) {
        if (uriText == null || uriText.trim().isEmpty()) {
            preview.setImageDrawable(null);
            preview.setContentDescription("No issue photo selected");
            return;
        }
        try {
            preview.setImageURI(Uri.parse(uriText));
            preview.setContentDescription("Issue photo attached");
        } catch (Exception e) {
            preview.setImageDrawable(null);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || pendingIssuePhotoIndex < 0) return;

        Uri uri = null;
        if (requestCode == REQUEST_PICK_ISSUE_PHOTO && data != null) {
            uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) { }
            }
        } else if (requestCode == REQUEST_TAKE_ISSUE_PHOTO) {
            uri = pendingCameraUri;
        }

        if (uri != null) {
            prefs.edit().putString("photo_" + pendingIssuePhotoIndex, uri.toString()).apply();
            if (pendingIssuePhotoPreview != null) updateIssuePhotoPreview(pendingIssuePhotoPreview, uri.toString());
            Toast.makeText(this, "Issue photo attached", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNote(int index, EditText notes) {
        prefs.edit().putString("notes_" + index, notes.getText().toString()).apply();
    }

    private void saveAllNotes() {
        android.content.SharedPreferences.Editor e = prefs.edit();
        for (ItemRow row : rows) e.putString("notes_" + row.index, row.notes.getText().toString());
        e.apply();
    }

    private void updateProgress() {
        int done = 0, issues = 0;
        for (int i=0; i<activeChecks.size(); i++) {
            int s = prefs.getInt("status_" + i, -1);
            if (s >= 0) done++;
            if (s == 1) issues++;
        }
        if (progressBar != null) progressBar.setProgress(done);
        if (progress != null) {
            String overall = done + " / " + activeChecks.size() + " checks complete" + (issues > 0 ? " • " + issues + " issue(s)" : "");
            if (openSection != null) overall += "\n" + openSection + ": " + sectionProgress(openSection);
            progress.setText(overall);
        }
    }

    private String buildReport() {
        saveAllNotes();
        StringBuilder sb = new StringBuilder();
        sb.append("Tesla ").append(selectedModel()).append(" Delivery Checklist Report\n");
        sb.append(new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.UK).format(new Date())).append("\n\n");
        String current = "";
        for (int i=0; i<activeChecks.size(); i++) {
            CheckItem item = activeChecks.get(i);
            if (!item.section.equals(current)) {
                current = item.section;
                sb.append("\n").append(current).append("\n");
            }
            int s = prefs.getInt("status_" + i, -1);
            String status = s == 0 ? "PASS" : s == 1 ? "ISSUE" : s == 2 ? "N/A" : "NOT CHECKED";
            String note = prefs.getString("notes_" + i, "").trim();
            sb.append("- [").append(status).append("] ").append(item.text);
            if (s == 1 && !note.isEmpty()) sb.append(" — ").append(note);
            if (s == 1) {
                String photo = prefs.getString("photo_" + i, "").trim();
                if (!photo.isEmpty()) sb.append(" — Photo attached: ").append(photo);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void shareReport() {
        String report = buildReport();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "TesSure Delivery Checklist Report");
        send.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(send, "Share checklist report"));
    }

    private String buildIssuesReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tesla ").append(selectedModel()).append(" Delivery Issues\n");
        sb.append(new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.UK).format(new Date())).append("\n\n");

        int total = totalIssueCount();
        sb.append(total).append(" issue").append(total == 1 ? "" : "s").append(" raised\n");

        ArrayList<String> sections = issueSections();
        for (String section : sections) {
            sb.append("\n").append(section).append("\n");
            int issueNumber = 0;
            for (int i=0; i<activeChecks.size(); i++) {
                if (prefs.getInt("status_" + i, -1) == 1 && activeChecks.get(i).section.equals(section)) {
                    issueNumber++;
                    String note = prefs.getString("notes_" + i, "").trim();
                    String photo = prefs.getString("photo_" + i, "").trim();
                    sb.append(issueNumber).append(". ").append(activeChecks.get(i).text).append("\n");
                    sb.append("   Issue: ").append(note.isEmpty() ? "No issue text added." : note).append("\n");
                    if (!photo.isEmpty()) sb.append("   Photo attached\n");
                }
            }
        }
        return sb.toString();
    }

    private ArrayList<Uri> issuePhotoUris() {
        ArrayList<Uri> photos = new ArrayList<>();
        for (int i=0; i<activeChecks.size(); i++) {
            if (prefs.getInt("status_" + i, -1) == 1) {
                String photo = prefs.getString("photo_" + i, "").trim();
                if (!photo.isEmpty()) photos.add(Uri.parse(photo));
            }
        }
        return photos;
    }

    private void shareIssuesReport() {
        if (totalIssueCount() == 0) {
            Toast.makeText(this, "No issues to export", Toast.LENGTH_SHORT).show();
            return;
        }

        String report = buildIssuesReport();
        ArrayList<Uri> photos = issuePhotoUris();
        Intent send;
        if (photos.isEmpty()) {
            send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
        } else {
            send = new Intent(Intent.ACTION_SEND_MULTIPLE);
            send.setType("image/*");
            send.putParcelableArrayListExtra(Intent.EXTRA_STREAM, photos);
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        send.putExtra(Intent.EXTRA_SUBJECT, "Tesla Delivery Issues");
        send.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(send, "Send issues to..."));
    }

    private void showIssues() {
        saveAllNotes();
        showIssueReviewPage(null);
    }

    private void showIssueReviewPage(String requestedSection) {
        rows.clear();
        progress = null;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(42), dp(18), dp(14));
        header.setBackgroundColor(Color.rgb(14, 17, 25));
        root.addView(header);

        TextView eyebrow = text("ISSUE REVIEW", 12, TESLA_RED, true);
        eyebrow.setLetterSpacing(0.12f);
        header.addView(eyebrow);

        TextView title = text("Work Through Issues", 25, TEXT, true);
        title.setPadding(0, dp(4), 0, dp(2));
        header.addView(title);

        ArrayList<String> issueSections = issueSections();
        int totalIssues = totalIssueCount();
        TextView summary = text(totalIssues + " issue" + (totalIssues == 1 ? "" : "s") + " raised", 15, MUTED, false);
        header.addView(summary);

        if (!issueSections.isEmpty()) {
            TextView sectionLabel = text("Choose issue section", 13, MUTED, true);
            sectionLabel.setPadding(0, dp(12), 0, dp(4));
            header.addView(sectionLabel);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setClipToPadding(false);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(10), dp(12), dp(92));
        scroll.addView(content);

        if (issueSections.isEmpty()) {
            TextView empty = text("No issues marked yet.", 17, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(18), dp(36), dp(18), dp(36));
            content.addView(empty);
        } else {
            final String[] selectedSection = { requestedSection != null && issueSections.contains(requestedSection) ? requestedSection : issueSections.get(0) };

            Spinner issueSpinner = new Spinner(this);
            issueSpinner.setBackground(rounded(SURFACE_2, dp(12), BORDER, 1));
            issueSpinner.setPadding(dp(12), 0, dp(12), 0);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, issueSections) {
                @Override public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    styleIssueSectionText(view, issueSections.get(position), false);
                    return view;
                }

                @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    styleIssueSectionText(view, issueSections.get(position), true);
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            issueSpinner.setAdapter(adapter);
            issueSpinner.setSelection(issueSections.indexOf(selectedSection[0]));
            issueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedSection[0] = issueSections.get(position);
                    renderIssueSection(content, selectedSection[0]);
                }

                @Override public void onNothingSelected(AdapterView<?> parent) { }
            });
            header.addView(issueSpinner, new LinearLayout.LayoutParams(-1, dp(52)));
        }

        Button shareIssues = primaryButton("Export / Send Issues");
        shareIssues.setOnClickListener(v -> shareIssuesReport());
        LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(-1, dp(48));
        shareParams.setMargins(0, dp(10), 0, 0);
        header.addView(shareIssues, shareParams);

        Button back = secondaryButton("Back to Checklist");
        back.setOnClickListener(v -> showChecklistPage());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, dp(48));
        backParams.setMargins(0, dp(8), 0, 0);
        header.addView(back, backParams);

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void styleIssueSectionText(TextView view, String section, boolean dropdown) {
        int issues = sectionIssueCount(section);
        view.setText("● " + issues + "  " + section);
        view.setTextColor(TEXT);
        view.setTextSize(16);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setPadding(dp(dropdown ? 16 : 12), dropdown ? dp(14) : 0, dp(12), dropdown ? dp(14) : 0);
        if (dropdown) view.setBackgroundColor(SURFACE_2);
    }

    private ArrayList<String> issueSections() {
        ArrayList<String> sections = new ArrayList<>();
        for (int i=0; i<activeChecks.size(); i++) {
            if (prefs.getInt("status_" + i, -1) == 1 && !sections.contains(activeChecks.get(i).section)) {
                sections.add(activeChecks.get(i).section);
            }
        }
        return sections;
    }

    private int totalIssueCount() {
        int count = 0;
        for (int i=0; i<activeChecks.size(); i++) if (prefs.getInt("status_" + i, -1) == 1) count++;
        return count;
    }

    private void renderIssueSection(LinearLayout content, String section) {
        content.removeAllViews();
        TextView heading = text(section, 22, TEXT, true);
        heading.setPadding(dp(4), dp(4), dp(4), dp(4));
        content.addView(heading);

        TextView helper = text(sectionIssueCount(section) + " issue" + (sectionIssueCount(section) == 1 ? "" : "s") + " in this section. Review these with Tesla staff.", 14, MUTED, false);
        helper.setPadding(dp(4), 0, dp(4), dp(12));
        content.addView(helper);

        int issueNumber = 0;
        for (int i=0; i<activeChecks.size(); i++) {
            if (prefs.getInt("status_" + i, -1) == 1 && activeChecks.get(i).section.equals(section)) {
                issueNumber++;
                addIssueSummaryCard(content, i, issueNumber, section);
            }
        }
    }

    private void addIssueSummaryCard(LinearLayout parent, int index, int issueNumber, String returnSection) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(rounded(SURFACE_2, dp(14), BORDER, 1));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(0, 0, 0, dp(12));
        parent.addView(card, cardParams);

        TextView heading = text("Issue " + issueNumber, 14, TESLA_RED, true);
        card.addView(heading);

        TextView itemText = text(activeChecks.get(index).text, 16, TEXT, true);
        itemText.setPadding(0, dp(6), 0, dp(8));
        card.addView(itemText);

        String note = prefs.getString("notes_" + index, "").trim();
        TextView noteText = text(note.isEmpty() ? "No issue text added." : note, 15, note.isEmpty() ? MUTED : TEXT, false);
        noteText.setPadding(0, 0, 0, dp(10));
        card.addView(noteText);

        String photo = prefs.getString("photo_" + index, "").trim();
        if (!photo.isEmpty()) {
            ImageView thumbnail = new ImageView(this);
            thumbnail.setImageURI(Uri.parse(photo));
            thumbnail.setAdjustViewBounds(true);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setBackground(rounded(SURFACE, dp(12), BORDER, 1));
            thumbnail.setContentDescription("Tap to view full-size issue photo");
            thumbnail.setOnClickListener(v -> showFullSizeIssuePhoto(photo, returnSection));
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(-1, dp(170));
            card.addView(thumbnail, imageParams);

            TextView tapHint = text("Tap photo to view full size", 13, MUTED, false);
            tapHint.setPadding(0, dp(6), 0, 0);
            card.addView(tapHint);
        } else {
            TextView noPhoto = text("No photo attached.", 13, MUTED, false);
            card.addView(noPhoto);
        }
    }

    private void showFullSizeIssuePhoto(String photoUri, String returnSection) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(42), dp(18), dp(14));
        header.setBackgroundColor(Color.BLACK);
        root.addView(header);

        TextView title = text("Issue Photo", 24, TEXT, true);
        header.addView(title);

        Button back = secondaryButton("Back to Issues");
        back.setOnClickListener(v -> showIssueReviewPage(returnSection));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, dp(48));
        backParams.setMargins(0, dp(10), 0, 0);
        header.addView(back, backParams);

        ImageView image = new ImageView(this);
        image.setImageURI(Uri.parse(photoUri));
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setBackgroundColor(Color.BLACK);
        image.setPadding(dp(4), dp(4), dp(4), dp(4));
        root.addView(image, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
            .setTitle("Reset checklist?")
            .setMessage("This clears all ticks, notes, and issue photos for the selected " + selectedModel() + " checklist.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Reset", (d, w) -> {
                android.content.SharedPreferences.Editor editor = prefs.edit();
                for (int i = 0; i < activeChecks.size(); i++) {
                    editor.remove("status_" + i);
                    editor.remove("notes_" + i);
                    editor.remove("photo_" + i);
                }
                editor.apply();
                showChecklistPage();
            })
            .show();
    }

    @Override public void onBackPressed() {
        CharSequence currentTitle = progress == null ? "" : progress.getText();
        if (currentTitle.length() > 0) {
            showLandingPage();
        } else {
            super.onBackPressed();
        }
    }
}
