	.inesprg 1   ; 1x 16KB PRG code
	.ineschr 0   ; 1x  8KB CHR data
	.inesmap 0   ; mapper 0 = NROM, no bank swapping
	.inesmir 0   ; background mirroring
  

;;;;;;;;;;;;;;;

    
	.bank 0
	.org $C


;set up our pointers
start:
	lda LOW(logo) ;for the logo
	sta $0
	lda HIGH(logo)
	sta $1

	lda #$00 ;and to the screen area
	sta $2
	lda #$10
	sta $3

	
	lda #$00
	sta $5
	
loop: ;main loop
	lda [$0],y ;load the repeat count
	cmp #0 ;if it is zero
	beq done ;we\'re done
	tax ;put the repeat count in x
	iny
	lda [$0],y ;now load the byte
	iny

rleloop:
	jsr draw ;go draw our current pixel using the color in a
	dex
	cpx #0 ;if x is zero
	beq loop ;we are done with this rle block
	jmp rleloop ;otherwise, output another pixel

draw:
	sty $4 ;store the current y and load the other y for this routine
	ldy $5
	sta [$2],y ;actually plot the pixel
	iny ;get ready for the next pixel
	cpy #00 ;if y has wrapped around
	beq next ;increment the address and reset y
	sty $5 ;save y and load the old one
	ldy $4
	rts

next:
	ldy #0 ;reset y
	inc $03 ;but increment the screen pointer
	sty $5 ;save y and load the old one
	ldy $4
	rts

done:

;RLE logo data
;each     .db is one RLE block
;RLE blocks are encoded as repeat count and then byte
;a count of zero indicates end of stream
logo:
    .db 45,1 ;for example, this says repeat 1 45 times
    .db 7,6 ;and repeat 6 7 times
    .db 22,1
    .db 11,6
    .db 19,1
    .db 13,6
    .db 18,1
    .db 14,6
    .db 17,1
    .db 15,6
    .db 16,1
    .db 16,6
    .db 15,1
    .db 17,6
    .db 14,1
    .db 12,6
    .db 4,1
    .db 2,6
    .db 14,1
    .db 10,6
    .db 8,1
    .db 11,6
    .db 2,1
    .db 9,6
    .db 10,1
    .db 10,6
    .db 3,1
    .db 8,6
    .db 11,1
    .db 9,6
    .db 4,1
    .db 8,6
    .db 11,1
    .db 8,6
    .db 4,1
    .db 8,6
    .db 12,1
    .db 7,6
    .db 5,1
    .db 8,6
    .db 12,1
    .db 6,6
    .db 6,1
    .db 8,6
    .db 12,1
    .db 5,6
    .db 7,1
    .db 8,6
    .db 12,1
    .db 5,2
    .db 7,1
    .db 8,6
    .db 12,1
    .db 6,2
    .db 6,1
    .db 8,6
    .db 12,1
    .db 7,2
    .db 5,1
    .db 8,6
    .db 12,1
    .db 8,2
    .db 5,1
    .db 8,6
    .db 11,1
    .db 9,2
    .db 4,1
    .db 9,6
    .db 10,1
    .db 10,2
    .db 4,1
    .db 9,6
    .db 9,1
    .db 11,2
    .db 3,1
    .db 10,6
    .db 7,1
    .db 1,6
    .db 15,1
    .db 17,6
    .db 15,1
    .db 17,6
    .db 16,1
    .db 16,6
    .db 17,1
    .db 15,6
    .db 19,1
    .db 13,6
    .db 20,1
    .db 12,6
    .db 23,1
    .db 9,6
    .db 43,1
    .db 0 ;end of stream marker





	.bank 1
	.org $FFFA
	.dw 0
	.dw start
	.dw 0  