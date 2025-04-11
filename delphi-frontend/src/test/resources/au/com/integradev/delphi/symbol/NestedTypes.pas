unit NestedTypes;

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

type
  TFlimFlam = class
    class procedure Flarp;
  end;

  TFoo = class(TFlimFlam)
    type
      TBar = class
        procedure Baz;
      end;
  end;

class procedure TFlimFlam.Flarp;
begin
  // do nothing
end;

procedure TFoo.TBar.Baz;
begin
  Flarp;
end;

end.