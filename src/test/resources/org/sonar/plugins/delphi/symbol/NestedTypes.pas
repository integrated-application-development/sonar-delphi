unit NestedTypes;

{This is a sample Delphi file.}

interface
  type
    TFoo = class
      type
        TBar = class
          class procedure DoStuff;
        end;
      type
        TBaz = class
          procedure Test;
        end;
    end;

implementation

procedure TFoo.TBaz.Test;
var
  A: TFoo.TBar;
  B: TBar;
begin
  A.DoStuff;
  B.DoStuff;
end;

end.