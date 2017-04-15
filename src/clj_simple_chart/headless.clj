(ns clj-simple-chart.headless)

(defn assign-private-static-field [cls name value]
  (let [field (.getDeclaredField cls name)]
    (.setAccessible field true)
    (.set field cls value)
    (.setAccessible field false)))

(defn init-headless []
  (System/setProperty "javafx.macosx.embedded" "true")
  (System/setProperty "glass.platform" "Monocle")
  (System/setProperty "monocle.platform" "Headless")
  (System/setProperty "prism.order" "sw")
  ; Monocle platform ...
  (let [platform-factory (Class/forName "com.sun.glass.ui.PlatformFactory")
        platform (.newInstance (Class/forName "com.sun.glass.ui.monocle.MonoclePlatformFactory"))]
    (assign-private-static-field platform-factory "instance" platform))
  ; Headless platform ...:
  (let [platform-factory (Class/forName "com.sun.glass.ui.monocle.NativePlatformFactory")
        platform (.newInstance (Class/forName "com.sun.glass.ui.monocle.headless.HeadlessPlatform"))]
    (assign-private-static-field platform-factory "platform" platform)))
