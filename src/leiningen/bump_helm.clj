(ns leiningen.bump-helm
  (:require [clojure.string :as str])
  (:import (java.io File)
           (java.util.regex Pattern)))

(def ^:private default-fields ["version"])

(defn- replace-version-field
  "Replaces the value of the given top level YAML key with new-version,
  preserving the original indentation and quoting style."
  [content field new-version]
  (str/replace content
               (re-pattern (format "(?m)^([ \\t]*%s:[ \\t]*)([\"']?)[^\"'\\r\\n]+([\"']?)[ \\t]*$"
                                   (Pattern/quote field)))
               (fn [[_ prefix open-quote close-quote]]
                 (str prefix open-quote new-version close-quote))))

(defn- resolve-chart-file
  "Resolves the configured chart path to a Chart.yaml file. When the path
  points at a chart directory, `Chart.yaml` inside it is used."
  [chart-path]
  (let [f (File. ^String chart-path)]
    (if (.isDirectory f)
      (.getPath (File. f "Chart.yaml"))
      chart-path)))

(defn bump-helm
  "Sets the current project version in a Helm Chart.yaml file.

  By default the `version` field is updated. Pass one or more field
  names to target different keys, e.g. `lein bump-helm appVersion` or
  `lein bump-helm version appVersion`.

  The chart location is read from `:bump-helm {:chart-path \"...\"}` in
  project.clj and may point at a `Chart.yaml` file or the chart directory
  that contains it. A chart path is required."
  [project & fields]
  (if-let [chart-path (get-in project [:bump-helm :chart-path])]
    (let [chart-file (resolve-chart-file chart-path)
          fields (or (seq fields) default-fields)
          version (:version project)]
      (if-not (.exists (File. ^String chart-file))
        (println (format "Error: helm chart file %s does not exist" chart-file))
        (let [updated (reduce (fn [content field]
                                (replace-version-field content field version))
                              (slurp chart-file)
                              fields)]
          (spit chart-file updated)
          (println (format "Set %s in %s to %s"
                           (str/join ", " fields) chart-file version)))))
    (println "Error: no chart path provided. Set :bump-helm {:chart-path \"...\"} in project.clj")))
