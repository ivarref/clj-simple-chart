(ns demo.refresh
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.tools.namespace.dependency :as ns-dep]
            [clojure.tools.namespace.file :as ns-file]
            [clojure.tools.namespace.track :as ns-track]))

(defn- find-sources
  "Finds a list of source files located in the given directories."
  [dirs]
  (->>
    dirs
    (remove nil?)
    (map io/file)
    (mapcat file-seq)
    (filter (some-fn ns-file/clojurescript-file?
                     ns-file/clojure-file?))
    (remove #(= "src/demo/refresh.clj" (str %)))))

(defn- file-deps
  "Calculate the dependency graph of the namespaces in the given files."
  [files]
  (->>
    files
    (ns-file/add-files {})))

(defn all-dependents
  [dependents-map ns-sym]
  {:pre [(symbol? ns-sym)
         (map? dependents-map)]}
  (reduce (fn [o child]
            (set/union o (all-dependents dependents-map child)))
          (get dependents-map ns-sym)
          (get dependents-map ns-sym)))

(defn all-dependents-height
  [dependents-map ns-sym height]
  {:pre [(symbol? ns-sym)
         (map? dependents-map)]}
  (let [children (->> (get dependents-map ns-sym)
                      (mapv (fn [child] {:dep child :height height}))
                      (into #{}))]
    (reduce (fn [o child]
              (set/union o (all-dependents-height dependents-map child (inc height))))
            children
            (get dependents-map ns-sym))))
(comment
  (all-dependents-height
    '{demo.mid #{demo.top}, demo.bottom #{demo.mid}}
    'demo.bottom 0))

(comment
  (all-dependents
    '{demo.mid #{demo.top}, demo.bottom #{demo.mid}}
    'demo.bottom))

(defn log [& args]
  (binding [*out* *err*]
    (apply println args)))

(defn refresh-to-root!
  [root from-ns]
  {:pre [(symbol? root)
         (symbol? from-ns)]}
  (let [dep-map (file-deps (find-sources ["src"]))
        filemap (get dep-map :clojure.tools.namespace.file/filemap)
        ns-sym->file (zipmap (vals filemap) (keys filemap))
        dependents-map (get-in dep-map [:clojure.tools.namespace.track/deps :dependents])
        all-deps-sorted (->> (all-dependents-height dependents-map from-ns 0)
                             (into [])
                             (sort-by :height)
                             (mapv :dep)
                             (into [from-ns])
                             (distinct))]
    (assert (contains? ns-sym->file root))
    (assert (contains? ns-sym->file from-ns))
    (let [refreshed (atom [])]
      (doseq [dep all-deps-sorted]
        (if (or (= dep root) (contains? (all-dependents dependents-map dep) root))
          (do
            ;(log "Loading" dep)
            (load-file (str (get ns-sym->file dep)))
            (swap! refreshed conj dep))
          (do
            ;(log "skipping" dep)
            nil)))
      (into [:loaded] @refreshed))))

(defonce root-ns-atom (atom nil))

(defn set-focus! [root-ns]
  (reset! root-ns-atom (symbol (str root-ns))))

(defn refresh-from-ns! [from-ns]
  (if-let [root-ns @root-ns-atom]
    (refresh-to-root! root-ns (symbol (str from-ns)))
    (do
      (log "Root-ns not set, please use set-focus!")
      nil #_(refresh-to-root! (symbol (str from-ns)) (symbol (str from-ns))))))
