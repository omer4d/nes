	.inesprg 1   ; 1x 16KB PRG code
	.ineschr 0   ; 1x  8KB CHR data
	.inesmap 0   ; mapper 0 = NROM, no bank swapping
	.inesmir 0   ; background mirroring
  

;;;;;;;;;;;;;;;

    
	.bank 0
	.org $0000

setcol:
	lda #2
	rts
	
start:

	ldx #0
	
	row_loop:
		; Load row:
		lda rows,x
		sta curr_row
		inx
		
		lda rows,x
		sta (curr_row + 1)
		inx
	
		ldy #0 ;init counter
		lda #2 ;set color
		jsr setcol
	
		col_loop:
			sta [curr_row],y
	
			iny
			cpy #32
			bne col_loop
		
	cpx #64-2
	bne row_loop
	


rows:
	.db $0,$2,$20,$2,$40,$2,$60,$2
	.db $80,$2,$a0,$2,$c0,$2,$e0,$2
	.db $0,$3,$20,$3,$40,$3,$60,$3
	.db $80,$3,$a0,$3,$c0,$3,$e0,$3
	.db $0,$4,$20,$4,$40,$4,$60,$4
	.db $80,$4,$a0,$4,$c0,$4,$e0,$4
	.db $0,$5,$20,$5,$40,$5,$60,$5
	.db $80,$5,$a0,$5,$c0,$5,$e0,$5

curr_row .dw 0

	.bank 1
	.org $FFFA
	.dw 0
	.dw start
	.dw 0  