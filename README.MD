## Пример использования части API.

```
var cfg = plugin.configManager();
for (var spec : cfg.items()) {
var stack = cfg.resolveItem(spec); // уже с кэшем
if (stack == null) continue;
// ...дальше открываете GUI/ставите цену spec.getPrice()
}

// локализованные строки
String title = plugin.messages().get("menu-title", Map.of("title", cfg.menuTitle()));
String desc  = plugin.messages().get("menu-description", Map.of("desc", cfg.menuDescription()));
```