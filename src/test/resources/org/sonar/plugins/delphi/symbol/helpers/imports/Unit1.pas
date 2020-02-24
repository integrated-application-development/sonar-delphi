unit Unit1;

{This is a sample Delphi file.}

interface

type
  TFoo = class
  end;
  
implementation

uses
    Unit2
  , Unit3
  , Unit4
  ;

procedure TestBarType(Bar: String);
begin
  // Do nothing
end;

procedure Test(Foo: TFoo);
begin
  TestBarType(Foo.Bar);
end;

end.