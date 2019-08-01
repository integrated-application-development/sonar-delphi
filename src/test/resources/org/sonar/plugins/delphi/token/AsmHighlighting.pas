unit AsmHighlighting;

{This is a sample Delphi file.}

interface

implementation

function TestKeywordCollisions(Red: Integer; Green: Integer; Blue: Integer): TColor;
var
  MyByte : Byte;
  MyBoolean: Boolean;
begin
  MyByte := $2F;

  // Make sure 'shl' gets highlighted
  MyByte := myByte shl $24;

  // Make sure 'shr' gets highlighted
  MyByte := myByte shr $24;

  // Make sure 'xor' gets highlighted
  MyBoolean := myByte xor $24;

  // Make sure 'and' gets highighted
  MyBoolean := (MyBoolean and MyByte = $24);

  // Make sure 'asm' and 'end' are highlighted, but nothing in between
  asm
    MOV  ecx,0
    MOV  eax,Red
    AND  eax,255
    SHL  eax,16
    XOR  ecx,eax
    MOV  eax,Green
    AND  eax,255
    SHL  eax,8
    XOR  ecx,eax
    MOV  eax,Blue
    AND  eax,255
    XOX  ecx,eax
    MOV  Result, ecx
  end;
end;

function TestAssemblerFunction(Red: Integer; Green: Integer; Blue: Integer): TColor; assembler;
asm
  MOV  ecx,0
  MOV  eax,Red
  AND  eax,255
  SHL  eax,16
  XOR  ecx,eax
  MOV  eax,Green
  AND  eax,255
  SHL  eax,8
  XOR  ecx,eax
  MOV  eax,Blue
  AND  eax,255
  XOX  ecx,eax
  MOV  Result, ecx
end;

function TestComments(Red: Integer; Green: Integer; Blue: Integer): TColor;
begin
  asm
    MOV  ecx,0      {ecx will hold the value of TColor}
    MOV  eax,Red    // start with the Red component}
    AND  eax,255    {make sure Red is in range 0<=Red<=255}
    SHL  eax,16     {shift the Red value to the correct position}
    XOR  ecx,eax    {adjust value of TColor}
    MOV  eax,Green  (* same again with Green component *)
    AND  eax,255
    SHL  eax,8
    XOR  ecx,eax
    MOV  eax,Blue   // and again with Blue
    AND  eax,255
    XOX  ecx,eax
    MOV  Result, ecx
  end;
end;

end.