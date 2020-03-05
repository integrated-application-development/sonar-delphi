unit MethodInterfaceNameResolution;

{This is a sample Delphi file.}

interface

type
  TFoo<T> = class
    procedure Test;
  end;

  TFoo = class
    procedure Test;
  end;

implementation

procedure TFoo<T>.Test;
begin
  // Do nothing
end;

procedure TFoo.Test;
begin
  // Do nothing
end;

end.