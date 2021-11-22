(ns build
  (:require [babashka.fs :as fs]
            [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'io.github.nextjournal/valuehash)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn jar [_]
  (b/delete {:path "target"})
  (println "Producing jar:" jar-file)
  (spit (doto (fs/file "resources/META-INF/nextjournal/valuehash/meta.edn")
          (-> fs/parent fs/create-dirs)) {:version version})
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :scm {:url "http://github.com/nextjournal/valuehash"
                      :connection "scm:git:git://github.com/nextjournal/valuehash.git"
                      :developerConnection "scm:git:ssh://git@github.com/nextjournal/valuehash.git"}
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn deploy [opts]
  (println "Deploying version" jar-file "to Clojars.")
  (jar {})
  (dd/deploy (merge {:installer :remote
                     :artifact jar-file
                     :pom-file (b/pom-path {:lib lib :class-dir class-dir})}
                    opts)))
