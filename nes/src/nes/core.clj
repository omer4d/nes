(ns nes.core)

(def instr-table
  [{:name "ADC"
    :op-type :r8
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
    :op-type :r8
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
    :op-type :rw8
    :modes
    [[:acc  10 2 false]
     [:zp   6  5 false]
     [:zpx  22 6 false]
     [:abs  14 6 false]
     [:absx 30 7 false]]}
   
   {:name "BCC", :op-type :r8, :modes [[:rel 144 2 false]]}
   {:name "BCS", :op-type :r8, :modes [[:rel 176 2 false]]}
   {:name "BEQ", :op-type :r8, :modes [[:rel 240 2 false]]}
   
   {:name "BIT"
    :op-type :r8
    :modes
    [[:zp  36  3 false]
     [:abs 44  4 false]]}
   
   {:name "BMI", :op-type :r8, :modes [[:rel 48  2 false]]}
   {:name "BNE", :op-type :r8, :modes [[:rel 208 2 false]]}
   {:name "BPL", :op-type :r8, :modes [[:rel 16  2 false]]}
   {:name "BRK", :op-type :na, :modes [[:imp 0   7 false]]}
   {:name "BVC", :op-type :r8, :modes [[:rel 80  2 false]]}
   {:name "BVS", :op-type :r8, :modes [[:rel 112 2 false]]}
   {:name "CLC", :op-type :na, :modes [[:imp 24  2 false]]}
   {:name "CLD", :op-type :na, :modes [[:imp 216 2 false]]}
   {:name "CLI", :op-type :na, :modes [[:imp 88  2 false]]}
   {:name "CLV", :op-type :na, :modes [[:imp 184 2 false]]}
   
   {:name "CMP",
    :op-type :r8
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
    :op-type :r8
    :modes
    [[:imm 224 2 false]
     [:zp  228 3 false]
     [:abs 236 4 false]]}
   
   {:name "CPY"
    :op-type :r8
    :modes
    [[:imm 192 2 false]
     [:zp  196 3 false]
     [:abs 204 4 false]]}
   
   {:name "DEC"
    :modes
    :op-type :rw8
    [[:zp   198 5 false]
     [:zpx  214 6 false]
     [:abs  206 6 false]
     [:absx 222 7 false]]}
   
   {:name "DEX", :op-type :na, :modes [[:imp 202 2 false]]}
   {:name "DEY", :op-type :na, :modes [[:imp 136 2 false]]}
   
   {:name "EOR"
    :op-type :r8
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
    :op-type :rw8
    :modes
    [[:zp   230 5 false]
     [:zpx  246 6 false]
     [:abs  238 6 false]
     [:absx 254 7 false]]}
   
   {:name "INX", :op-type :na, :modes [[:imp 232 2 false]]}
   {:name "INY", :op-type :na, :modes [[:imp 200 2 false]]}
   
   {:name "JMP"
    :op-type :r16
    :modes
    [[:abs 76  3 false]
     [:ind 108 5 false]]}
 
   {:name "JSR", :op-type :r16, :modes [[:abs 32 6 false]]}
   
   {:name "LDA",
    :op-type :r8
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
    :op-type :r8
    :modes
    [[:imm  162 2 false]
     [:zp   166 3 false]
     [:zpy  182 4 false]
     [:abs  174 4 false]
     [:absy 190 4 true]]}
   
   {:name "LDY"
    :op-type :r8
    :modes
    [[:imm  160 2 false]
     [:zp   164 3 false]
     [:zpx  180 4 false]
     [:abs  172 4 false]
     [:absx 188 4 true]]}
   
   {:name "LSR"
    :op-type :rw8
    :modes
    [[:acc  74 2 false]
     [:zp   70 5 false]
     [:zpx  86 6 false]
     [:abs  78 6 false]
     [:absx 94 7 false]]}
   
   {:name "NOP", :op-type :na, :modes [[:imp 234 2 false]]}
 
   {:name "ORA",
    :op-type :r8
    :modes
    [[:imm  9  2 false]
     [:zp   5  3 false]
     [:zpx  21 4 false]
     [:abs  13 4 false]
     [:absx 29 4 true]
     [:absy 25 4 true]
     [:indx 1  6 false]
     [:indy 17 5 true]]}
   
   {:name "PHA", :op-type :na, :modes [[:imp 72 3 false]]}
   {:name "PHP", :op-type :na, :modes [[:imp 8 3 false]]}
   {:name "PLA", :op-type :na, :modes [[:imp 104 4 false]]}
   {:name "PLP", :op-type :na, :modes [[:imp 40 4 false]]}
   
   {:name "ROL"
    :op-type :rw8
    :modes
    [[:acc  42 2 false]
     [:zp   38 5 false]
     [:zpx  54 6 false]
     [:abs  46 6 false]
     [:absx 62 7 false]]}
   
   {:name "ROR"
    :op-type :rw8
    :modes
    [[:acc  106 2 false]
     [:zp   102 5 false]
     [:zpx  118 6 false]
     [:abs  110 6 false]
     [:absx 126 7 false]]}
   
   {:name "RTI", :op-type :na, :modes [[:imp 64 6 false]]}
   {:name "RTS", :op-type :na, :modes [[:imp 96 6 false]]}
   
   {:name "SBC"
    :op-type :r8
    :modes
    [[:imm  233 2 false]
     [:zp   229 3 false]
     [:zpx  245 4 false]
     [:abs  237 4 false]
     [:absx 253 4 true]
     [:absy 249 4 true]
     [:indx 225 6 false]
     [:indy 241 5 true]]}
   
   {:name "SEC", :op-type :na, :modes [[:imp 56 2 false]]}
   {:name "SED", :op-type :na, :modes [[:imp 248 2 false]]}
   {:name "SEI", :op-type :na, :modes [[:imp 120 2 false]]}
   
   {:name "STA",
    :op-type :w8
    :modes
    [[:zp   133 3 false]
     [:zpx  149 4 false]
     [:abs  141 4 false]
     [:absx 157 5 false]
     [:absy 153 5 false]
     [:indx 129 6 false]
     [:indy 145 6 false]]}
   
   {:name "STX"
    :op-type :w8
    :modes
    [[:zp  134 3 false]
     [:zpy 150 4 false]
     [:abs 142 4 false]]}
   
   {:name "STY"
    :op-type :w8
    :modes
    [[:zp  132 3 false]
     [:zpx 148 4 false]
     [:abs 140 4 false]]}
   
   {:name "TAX", :op-type :na, :modes [[:imp 170 2 false]]}
   {:name "TAY", :op-type :na, :modes [[:imp 168 2 false]]}
   {:name "TSX", :op-type :na, :modes [[:imp 186 2 false]]}
   {:name "TXA", :op-type :na, :modes [[:imp 138 2 false]]}
   {:name "TXS", :op-type :na, :modes [[:imp 154 2 false]]}
   {:name "TYA", :op-type :na, :modes [[:imp 152 2 false]]}])
