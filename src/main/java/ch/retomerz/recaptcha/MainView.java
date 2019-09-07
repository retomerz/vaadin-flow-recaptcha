package ch.retomerz.recaptcha;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

  public MainView() {

    final ReCaptcha reCaptcha = new ReCaptcha(
            "Your site key",
            "Your secret key"
    );
    add(reCaptcha);

    Button button = new Button("Validate", (ComponentEventListener<ClickEvent<Button>>) event -> {
      boolean valid = reCaptcha.isValid();
      Notification notification = new Notification(valid ? "Valid" : "Not valid");
      notification.setDuration(2000);
      notification.setPosition(Notification.Position.MIDDLE);
      notification.addThemeVariants(valid ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
      notification.open();
    });
    add(button);
  }
}
