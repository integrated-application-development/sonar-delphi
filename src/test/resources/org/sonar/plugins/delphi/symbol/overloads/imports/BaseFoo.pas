unit BaseFoo;

{This is a sample Delphi file.}

interface

type
  TDistantFoo = class(TObject)
    procedure Bar(Baz: String); overload; virtual;
    procedure Bar(Baz: Integer); overload;
  end;
  
  TBaseFoo = class(TDistantFoo);

implementation

procedure TDistantFoo.Bar(Baz: String);
begin
  // Do nothing
end;

procedure TDistantFoo.Bar(Baz: Integer);
begin
  // Do nothing
end;

end.