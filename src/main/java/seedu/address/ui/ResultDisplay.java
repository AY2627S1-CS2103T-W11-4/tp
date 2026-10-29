package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final double HEIGHT_ROUNDING_ALLOWANCE = 1;

    @FXML
    private TextArea resultDisplay;

    /**
     * Creates a result display whose preferred height follows its wrapped feedback.
     */
    public ResultDisplay() {
        super(FXML);
        // Install the standard skin before reading its scroll pane's padding.
        resultDisplay.setSkin(new TextAreaSkin(resultDisplay));
        configureHeight();
        // Resizing happens during layout; request another pass after wrapping changes the height.
        resultDisplay.widthProperty().addListener(observable -> Platform.runLater(getRoot()::requestLayout));
    }

    /**
     * Measures wrapped feedback using the styled text area's font and content padding.
     * The parent window limits the height, so unusually long output remains scrollable.
     */
    private void configureHeight() {
        ScrollPane scrollPane = (ScrollPane) resultDisplay.getChildrenUnmodifiable().get(0);
        ScrollPaneSkin scrollSkin = new ScrollPaneSkin(scrollPane);
        scrollPane.setSkin(scrollSkin);
        ScrollBar verticalBar = scrollSkin.getVerticalScrollBar();
        Region content = (Region) scrollPane.getContent();
        Text measurement = new Text();
        // Match TextAreaSkin's paragraph metrics, including the space above and below each line.
        measurement.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        resultDisplay.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            measurement.setText(resultDisplay.getText());
            measurement.setFont(resultDisplay.getFont());
            // Reserve the styled scrollbar width even while it is hidden, so overflow cannot add a wrapped line.
            measurement.setWrappingWidth(Math.max(1, resultDisplay.getWidth()
                    - resultDisplay.snappedLeftInset() - resultDisplay.snappedRightInset()
                    - scrollPane.snappedLeftInset() - scrollPane.snappedRightInset()
                    - content.snappedLeftInset() - content.snappedRightInset()
                    - scrollPane.snapSizeX(verticalBar.prefWidth(-1))));
            // Leave a small allowance before snapping to avoid fractional-pixel overflow at 125% scaling.
            return content.snapSizeY(measurement.getLayoutBounds().getHeight() + HEIGHT_ROUNDING_ALLOWANCE)
                    + content.snappedTopInset() + content.snappedBottomInset()
                    + scrollPane.snappedTopInset() + scrollPane.snappedBottomInset()
                    + resultDisplay.snappedTopInset() + resultDisplay.snappedBottomInset();
        }, resultDisplay.textProperty(), resultDisplay.fontProperty(), resultDisplay.widthProperty(),
                content.insetsProperty(), scrollPane.insetsProperty(), resultDisplay.insetsProperty(),
                verticalBar.prefWidthProperty(), verticalBar.insetsProperty(), scrollPane.viewportBoundsProperty(),
                resultDisplay.sceneProperty()));
        getRoot().prefHeightProperty().bind(Bindings.createDoubleBinding(() ->
                resultDisplay.getPrefHeight() + getRoot().snappedTopInset() + getRoot().snappedBottomInset(),
                resultDisplay.prefHeightProperty(), getRoot().insetsProperty()));
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        resultDisplay.positionCaret(0);
        resultDisplay.setScrollTop(0);
    }

}
