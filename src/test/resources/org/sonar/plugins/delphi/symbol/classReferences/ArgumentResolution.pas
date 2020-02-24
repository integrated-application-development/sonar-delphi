unit ArgumentResolution;

{This is a sample Delphi file.}

interface

type
  TFoo = class(TObject)
  public
    class procedure Baz;
  end;

  TBar = class(TFoo)
  end;

  TMetaFoo = class of TFoo;

implementation

procedure CallBaz(Meta: TMetaFoo);
begin
  Baz.Baz;
end;

procedure Test;
begin
  CallBaz(TFoo);
  CallBaz(TBar);
end;


end.