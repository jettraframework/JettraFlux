# JettraFlux Guide

Welcome to **JettraFlux**, a modern, declarative, and highly themeable UI framework for the Jettra stack.
Inspired by Flutter and Jetpack Compose, FluxUI allows you to build rich user interfaces dynamically from the backend using an immutable and builder-pattern approach.

## 1. Core Concepts
In FluxUI, **Everything is a Widget**. You compose your UI by nesting Widgets.

Instead of manipulating a DOM-like structure iteratively, you declare the desired UI state:

```java
import io.jettra.flux.widgets.*;
import io.jettra.flux.core.Modifier;

Widget myUI = Scaffold.of()
    .topBar(
        Container.of(Text.of("My App")).modifier(new Modifier().background("#000").padding(16))
    )
    .body(
        Center.of(
            Column.of(
                Text.of("Welcome to FluxUI!"),
                TextField.of("username", "Enter your username"),
                ElevatedButton.of("Login").onClick(e -> System.out.println("Login clicked!"))
            )
        )
    );
```

## 2. Rendering and Themes
To render your UI in a JettraServer handler, simply call `render()` passing a `ThemeData`:

```java
import io.jettra.flux.theme.Themes;

public class MyHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String html = myUI.render(Themes.FuturisticTheme());
        
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, html.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(html.getBytes());
        }
    }
}
```

## 3. Supported Themes
FluxUI supports advanced theming out of the box:

1. **FlatTheme:** Clean, minimalistic, modern (Material Design style).
   `Themes.FlatTheme()`
2. **Theme3D:** Neumorphism/Skeuomorphism with soft inner and outer shadows creating volume.
   `Themes.Theme3D()`
3. **FuturisticTheme:** Dark background with neon glowing borders, perfect for Cyberpunk-style dashboards.
   `Themes.FuturisticTheme()`
4. **AstTheme:** Deep cosmic violet & turquoise theme.
   `Themes.AstTheme()`
5. **AtlantisTheme:** Clean enterprise slate and blue theme.
   `Themes.AtlantisTheme()`
6. **OceanTheme:** Modern dark theme with mint green and cyan accents.
   `Themes.OceanTheme()`
7. **Retro / RetroTheme:** Iconic Minecraft aesthetic with pixelated typography, 3D stone/wood beveled buttons, crafting GUI cards, sunken inventory slots, and bedrock/grass/emerald color palette.
   `Themes.RetroTheme()` or `Themes.Retro()`

You can also create your own theme by instantiating `ThemeData` directly.

## 4. Modifiers
The `Modifier` class is used to decorate widgets with padding, margin, width, height, and backgrounds without polluting the widget's constructor:

```java
Text.of("Hello")
    .modifier(new Modifier().padding(10).background("#F00").cssClass("my-custom-class"));
```

## 5. Event Handling
FluxUI supports attaching click events directly using `.onClick(lambda)`. 
Behind the scenes, it generates an `onclick="fluxFire('id')"` HTML attribute.
To fully implement the backend loop, JettraFlux relies on an AJAX/Fetch script (to be included in your base HTML) that listens for these events and sends them back to the JettraServer, invoking your lambdas seamlessly.

---
*Built with ❤️ for the Jettra Ecosystem.*

## 6. Component Library Reference

FluxUI provides a rich set of declarative components. 

### Basic Elements & Layout
- **[Div](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/div.md)**: A multi-child general container (`<div>`).
- **[Span](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/span.md)**: An inline container (`<span>`).
- **[Paragraph](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/paragraph.md)**: A block text container (`<p>`).
- **[Header](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/header.md)**: Headers `<h1>` through `<h6>`.
- **View**: A structural element designed to fill 100% width and height of its parent.
  ```java
  View.of(Text.of("Full view content"));
  ```
- **Scroll**: A container that handles overflow (x, y, or both).
  ```java
  Scroll.of(Text.of("Scrollable content")).direction("y");
  ```

### Media & Visuals
- **[Icon](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/icon.md)**: Renders `<i>` tags for FontAwesome or similar icon fonts.
- **[Image](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/image.md)**: Renders `<img>` with `src` and `alt`.
- **Carousel**: A flex-based horizontal scrolling carousel.
  ```java
  Carousel.of(Image.of("img1.png"), Image.of("img2.png"));
  ```
- **ProgressBar**: An HTML5 `<progress>` bar.
  ```java
  ProgressBar.create().value(50).max(100);
  ```

### Navigation & Action
- **[Link](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/link.md)**: Renders `<a>` anchors with URL targets.
- **ElevatedButton / OutlinedButton**: Material-style buttons for actions.

### Forms & Inputs
- **[Form](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/form.md)**: Form container with `action` and `method`.
- **[Label](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/label.md)**: Label for form inputs (`forId`).
- **[TextArea](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/textarea.md)**: Multi-line text input.
- **[RadioButton](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/guide/examples/radiobutton.md)**: Standard radio selection input.
- **Checkbox / TextField**: Other common inputs.

### Advanced Data & Layout
- **Datatable**: Renders a semantic `<table>`.
  ```java
  Datatable.of(
      Arrays.asList("ID", "Name"), 
      Arrays.asList(Arrays.asList("1", "Alice"), Arrays.asList("2", "Bob"))
  );
  ```
- **Login**: A pre-built authentication form container.
  ```java
  Login.create().action("/auth").title("Sign In to MyApp");
  ```
- **Modal**: An overlay container for popup content.
  ```java
  Modal.of(Text.of("Modal Content")).open(true);
  ```
- **Dialog**: An HTML5 `<dialog>` component.
  ```java
  Dialog.of(Text.of("Dialog details")).open(true);
  ```

### Extended Components (Phase 3)
A massive expansion of components was generated automatically to support full `JettraWUI` feature parity. These components are simple wrappers ready for custom implementation and have basic documentation in `guide/examples/`:

- **Layout & Structure**: `Board`, `Dashboard`, `Divide`, `Footer`, `Left`, `Page`, `Panel`, `TabView`, `Top`.
- **Media & Charts**: `Chart`, `ChartsBar`, `ChartsDoughnut`, `ChartsLine`, `ChartsPie`, `ChartsRadar`, `Draw`, `PDFViewer`, `QR`, `QRReader`, `BarCode`, `ViewMedia`.
- **Forms & Data**: `Catcha`, `CreditCard`, `DatePicker`, `TimePicker`, `FileUpload`, `FolderSelector`, `Inputs`, `OTPValidator`, `SelectMany`, `SelectOne`, `SelectOneIcon`, `ToggleSwitch`.
- **UI Feedback & Tools**: `Alert`, `Console`, `Downloader`, `Loading`, `Notification`, `Spinner`, `SessionTimeOutDialog`, `TrafficLight`.
- **Navigation & Complex**: `Avatar`, `AvatarGroup`, `CrudView`, `IndexUI`, `LoginAdvanced`, `LoginUI`, `Map`, `Menu`, `MenuBar`, `MenuItem`, `Organigram`, `Schedule`, `TimeLine`, `Tree`.
- **Base Components**: `Button`, `Calendar`, `CheckBoxGroup`, `Clock`, `FormGroup`, `RadioGroupButton`, `Script`, `Separator`, `Style`, `TD`, `TextBox`, `Time`.

Each of these components has a corresponding `.md` file in `guide/examples/` (e.g., `guide/examples/avatar.md`) providing a basic starting point for usage.
