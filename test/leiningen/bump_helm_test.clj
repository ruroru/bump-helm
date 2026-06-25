(ns leiningen.bump-helm-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest testing is]]
            [mock-clj.core :as mock]
            [leiningen.bump-helm :as bump-helm]))

(def ^:private chart-file "test/resources/Chart.yaml")
(def ^:private chart-dir "test/resources")

(defn- project
  ([version] (project version chart-file))
  ([version chart-path] {:version version :bump-helm {:chart-path chart-path}}))

(def ^:private chart-content
  (str "apiVersion: v2\n"
       "name: my-app\n"
       "description: A Helm chart for Kubernetes\n"
       "type: application\n"
       "version: 0.1.0\n"
       "appVersion: \"0.0.1\"\n"))

(defn- chart-with [version app-version]
  (str "apiVersion: v2\n"
       "name: my-app\n"
       "description: A Helm chart for Kubernetes\n"
       "type: application\n"
       "version: " version "\n"
       "appVersion: \"" app-version "\"\n"))

(deftest set-version-in-chart
  (testing "default updates version field and preserves appVersion"
    (mock/with-mock
      [spit  nil
       slurp chart-content]
      (bump-helm/bump-helm (project "1.2.3"))
      (is (= 1 (mock/call-count spit)))
      (is (= [(list chart-file (chart-with "1.2.3" "0.0.1"))]
             (mock/calls spit)))))

  (testing "can target appVersion, preserving quotes"
    (mock/with-mock
      [spit  nil
       slurp chart-content]
      (bump-helm/bump-helm (project "1.2.3") "appVersion")
      (is (= [(list chart-file (chart-with "0.1.0" "1.2.3"))]
             (mock/calls spit)))))

  (testing "can target multiple fields at once"
    (mock/with-mock
      [spit  nil
       slurp chart-content]
      (bump-helm/bump-helm (project "1.2.3") "version" "appVersion")
      (is (= [(list chart-file (chart-with "1.2.3" "1.2.3"))]
             (mock/calls spit)))))

  (testing "handles SNAPSHOT versions"
    (mock/with-mock
      [spit  nil
       slurp chart-content]
      (bump-helm/bump-helm (project "1.2.3-SNAPSHOT"))
      (is (= [(list chart-file (chart-with "1.2.3-SNAPSHOT" "0.0.1"))]
             (mock/calls spit)))))

  (testing "resolves a chart directory to its Chart.yaml"
    (let [resolved (.getPath (io/file chart-dir "Chart.yaml"))]
      (mock/with-mock
        [spit  nil
         slurp chart-content]
        (bump-helm/bump-helm (project "1.2.3" chart-dir))
        (is (= [(list resolved (chart-with "1.2.3" "0.0.1"))]
               (mock/calls spit)))))))

(deftest missing-chart-file
  (testing "does nothing when the chart file is absent"
    (mock/with-mock
      [spit  nil
       slurp chart-content]
      (bump-helm/bump-helm (project "1.2.3" "does/not/exist/Chart.yaml"))
      (is (= 0 (mock/call-count spit))))))

(deftest missing-chart-path
  (testing "prints an error and writes nothing when no chart path is provided"
    (mock/with-mock
      [spit  nil
       slurp chart-content]
      (let [output (with-out-str
                     (bump-helm/bump-helm {:version "1.2.3"}))]
        (is (= (str "Error: no chart path provided. Set :bump-helm {:chart-path \"...\"} in project.clj"
                    (System/lineSeparator))
               output))
        (is (= 0 (mock/call-count spit)))))))
