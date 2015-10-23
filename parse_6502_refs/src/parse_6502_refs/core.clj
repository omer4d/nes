(ns parse-6502-refs.core
  (:require [net.cgrand.enlive-html :as html] [clojure.string :as str])
  (:use clojure.pprint))

(use 'clojure.pprint)
(use 'clojure.data)

(defn fetch-url [url]
  (html/html-resource (java.net.URL. "http://homepage.ntlworld.com/cyborgsystems/CS_Main/6502/6502.htm")))

(defn fetch-url-2 [url]
  (html/html-resource (clojure.java.io/resource "ref3.html")))


;  (html/html-resource (java.io.StringReader.
;                     "<table bgcolor='#008000'>
;     <tr><td><big><big><b>ADC</b></big></big></td></tr>
; </table>")))

  


;(let [sel [[:table (html/attr= :bgcolor "#008000")] :> [:tr (html/nth-of-type 1)] :td :font]]
;  (pprint (map html/text (take 30 (html/select (fetch-url *base-url*) sel)))))


(defn proc-tbl [tbl]
  (html/select tbl
               #{[[:tr (html/nth-of-type 1)] :> :td :big :> :b]
                 [[:tr (html/nth-of-type 1)] :> :td :> :pre :> :big :>
                  #{:x [:font (html/but (html/has [:u]))]}]}))

(defn translate-mode-name [name]
  (case name
    "Immediate"    :imm
    "Implied"      :imp
    "Accumulator"  :acc
    "Zero Page"    :zp
    "Zero Page,X"  :zpx
    "Zero Page,Y"  :zpy
    "Absolute"     :abs
    "Absolute,X"   :absx
    "Absolute,Y"   :absy
    "(Indirect,X)" :indx
    "(Indirect),Y" :indy
    "Indirect"     :ind
    "Indirect_"    :ind
    "Relative"     :rel))

(defn proc-row [row]
  (let [[mode syntax code bytes cycles]
      (str/split row #"   +")]
  [(translate-mode-name mode)
   (read-string (str/replace code #"\$" "0x"))
   (read-string (str/replace cycles #"[^0-9]" ""))
   (.contains cycles "+")]))

(defn proc-row-2 [[mode code bytes cycles]]
  [(translate-mode-name (str/replace (str/replace mode #"\n" "") #" +" " "))
   (read-string (str/replace (str/replace code #"[\n ]" "") #"\$" "0x"))
   (read-string (str/replace cycles #"\(.*\)| " ""))
   (.contains cycles "+")])

(defn text-tbl->map [[name & rows]]
  {:name name
   :modes (into [] (map proc-row rows))})
  
  
(defn build1 []
  (let [tbl-sel [[:table (html/attr= :bgcolor "#008000")]]]
    (map (comp text-tbl->map #(map html/text (proc-tbl %))) (take 56 (html/select (fetch-url nil) tbl-sel)))))


(defn build2 []
  (let [tables (html/select (fetch-url-2 nil) [[:table (html/attr= :width "450") (html/has [:b])]])]
    (map (fn [modes name] {:name name :modes (into [] modes)})
         (map #(->> (map html/text (html/select % [:td]))
                    (drop 4)
                    (partition 4)
                    (map proc-row-2))
              (take 56 tables))
         (map #(get-in % [:attrs :name]) (html/select (fetch-url-2 nil) [:h3 :a])))))

;(build2)


                                        ;(pprint )

;(diff (build1) (build2))

;<h3><a name="AND"></a>AND - Logical AND</h3>



(def baz [{:name "ADC"
  :modes
  [[:imm  105 2 false]
   [:zp   101 3 false]
   [:zpx  117 4 false]
   [:abs  109 4 false]
   [:absx 125 4 true]
   [:absy 121 4 true]
   [:indx 97  6 false]
   [:indy 113 5 true]]}
 
 {:name "AND"
  :modes
  [[:imm  41 2 false]
   [:zp   37 3 false]
   [:zpx  53 4 false]
   [:abs  45 4 false]
   [:absx 61 4 true]
   [:absy 57 4 true]
   [:indx 33 6 false]
   [:indy 49 5 true]]}
 
 {:name "ASL"
  :modes
  [[:acc  10 2 false]
   [:zp   6  5 false]
   [:zpx  22 6 false]
   [:abs  14 6 false]
   [:absx 30 7 false]]}
 
 {:name "BCC", :modes [[:rel 144 2 true]]}
 {:name "BCS", :modes [[:rel 176 2 true]]}
 {:name "BEQ", :modes [[:rel 240 2 true]]}
 
 {:name "BIT"
  :modes
  [[:zp  36  3 false]
   [:abs 44  4 false]]}
 
 {:name "BMI", :modes [[:rel 48  2 true]]}
 {:name "BNE", :modes [[:rel 208 2 true]]}
 {:name "BPL", :modes [[:rel 16  2 true]]}
 {:name "BRK", :modes [[:imp 0   7 false]]}
 {:name "BVC", :modes [[:rel 80  2 true]]}
 {:name "BVS", :modes [[:rel 112 2 true]]}
 {:name "CLC", :modes [[:imp 24  2 false]]}
 {:name "CLD", :modes [[:imp 216 2 false]]}
 {:name "CLI", :modes [[:imp 88  2 false]]}
 {:name "CLV", :modes [[:imp 184 2 false]]}
 
 {:name "CMP",
  :modes
  [[:imm  201 2 false]
   [:zp   197 3 false]
   [:zpx  213 4 false]
   [:abs  205 4 false]
   [:absx 221 4 true]
   [:absy 217 4 true]
   [:indx 193 6 false]
   [:indy 209 5 true]]}
 
 {:name "CPX"
  :modes
  [[:imm 224 2 false]
   [:zp  228 3 false]
   [:abs 236 4 false]]}
 
 {:name "CPY"
  :modes
  [[:imm 192 2 false]
   [:zp  196 3 false]
   [:abs 204 4 false]]}
 
 {:name "DEC"
  :modes
  [[:zp   198 5 false]
   [:zpx  214 6 false]
   [:abs  206 6 false]
   [:absx 222 7 false]]}
 
 {:name "DEX", :modes [[:imp 202 2 false]]}
 {:name "DEY", :modes [[:imp 136 2 false]]}
 
 {:name "EOR"
  :modes
  [[:imm  73 2 false]
   [:zp   69 3 false]
   [:zpx  85 4 false]
   [:abs  77 4 false]
   [:absx 93 4 true]
   [:absy 89 4 true]
   [:indx 65 6 false]
   [:indy 81 5 true]]}
 
 {:name "INC"
  :modes
  [[:zp   230 5 false]
   [:zpx  246 6 false]
   [:abs  238 6 false]
   [:absx 254 7 false]]}

 {:name "INX", :modes [[:imp 232 2 false]]}
 {:name "INY", :modes [[:imp 200 2 false]]}
 
 {:name "JMP"
  :modes
  [[:abs 76  3 false]
   [:ind 108 5 false]]}
 
 {:name "JSR", :modes [[:abs 32 6 false]]}
 
 {:name "LDA",
  :modes
  [[:imm  169 2 false]
   [:zp   165 3 false]
   [:zpx  181 4 false]
   [:abs  173 4 false]
   [:absx 189 4 true]
   [:absy 185 4 true]
   [:indx 161 6 false]
   [:indy 177 5 true]]}
 
 {:name "LDX"
  :modes
  [[:imm  162 2 false]
   [:zp   166 3 false]
   [:zpy  182 4 false]
   [:abs  174 4 false]
   [:absy 190 4 true]]}
 
 {:name "LDY"
  :modes
  [[:imm  160 2 false]
   [:zp   164 3 false]
   [:zpx  180 4 false]
   [:abs  172 4 false]
   [:absx 188 4 true]]}
 
 {:name "LSR"
  :modes
  [[:acc  74 2 false]
   [:zp   70 5 false]
   [:zpx  86 6 false]
   [:abs  78 6 false]
   [:absx 94 7 false]]}

 {:name "NOP", :modes [[:imp 234 2 false]]}
 
 {:name "ORA",
  :modes
  [[:imm  9  2 false]
   [:zp   5  3 false]
   [:zpx  21 4 false]
   [:abs  13 4 false]
   [:absx 29 4 true]
   [:absy 25 4 true]
   [:indx 1  6 false]
   [:indy 17 5 true]]}
 
 {:name "PHA", :modes [[:imp 72 3 false]]}
 {:name "PHP", :modes [[:imp 8 3 false]]}
 {:name "PLA", :modes [[:imp 104 4 false]]}
 {:name "PLP", :modes [[:imp 40 4 false]]}
 
 {:name "ROL"
  :modes
  [[:acc  42 2 false]
   [:zp   38 5 false]
   [:zpx  54 6 false]
   [:abs  46 6 false]
   [:absx 62 7 false]]}
 
 {:name "ROR"
  :modes
  [[:acc  106 2 false]
   [:zp   102 5 false]
   [:zpx  118 6 false]
   [:abs  110 6 false]
   [:absx 126 7 false]]}
 
 {:name "RTI", :modes [[:imp 64 6 false]]}
 {:name "RTS", :modes [[:imp 96 6 false]]}
 
 {:name "SBC"
  :modes
  [[:imm  233 2 false]
   [:zp   229 3 false]
   [:zpx  245 4 false]
   [:abs  237 4 false]
   [:absx 253 4 true]
   [:absy 249 4 true]
   [:indx 225 6 false]
   [:indy 241 5 true]]}
 
 {:name "SEC", :modes [[:imp 56 2 false]]}
 {:name "SED", :modes [[:imp 248 2 false]]}
 {:name "SEI", :modes [[:imp 120 2 false]]}
 
 {:name "STA",
  :modes
  [[:zp   133 3 false]
   [:zpx  149 4 false]
   [:abs  141 4 false]
   [:absx 157 5 false]
   [:absy 153 5 false]
   [:indx 129 6 false]
   [:indy 145 6 false]]}

 {:name "STX"
  :modes
  [[:zp  134 3 false]
   [:zpy 150 4 false]
   [:abs 142 4 false]]}
 
 {:name "STY"
  :modes
  [[:zp  132 3 false]
   [:zpx 148 4 false]
   [:abs 140 4 false]]}
 
 {:name "TAX", :modes [[:imp 170 2 false]]}
 {:name "TAY", :modes [[:imp 168 2 false]]}
 {:name "TSX", :modes [[:imp 186 2 false]]}
 {:name "TXA", :modes [[:imp 138 2 false]]}
 {:name "TXS", :modes [[:imp 154 2 false]]}
 {:name "TYA", :modes [[:imp 152 2 false]]}])



(let [a baz
      b (build2)]
  (dorun (map (fn [ent1 ent2 idx]
                (let [modes1 (:modes ent1)
                      modes2 (:modes ent2)]
                  (when (not= modes1 modes2)
                    (println idx)
                    (println (:name ent1))
                    (println modes1)
                    (println modes2)
                    (println "--------------------------------"))))
                a b (range)))) 
