unit DoubleAmpersands;

interface

type
  &&TFoo = class(TObject)
    procedure MyProc;
    procedure &&MyProc(&&MyParam: &&TFoo);
    procedure &Begin;
  end;

implementation

{ TFoo }

procedure &&TFoo.&&MyProc(&&MyParam: &&TFoo);
var
  &&MyVar: &&TFoo;
  MyVar: &&TFoo;
begin
  // Note that &&MyParam is a compiler error if used
  &&MyVar := Self;
  MyVar := &&MyVar;
end;

procedure &&TFoo.&Begin;
begin

end;

procedure &&TFoo.MyProc;
begin

end;

end.
