unit Overrides;

interface

uses
  BaseFoo;

type
  TByteSet = set of Byte;

  TFoo = class(TBaseFoo)
    procedure Bar(Baz: String); override;
    procedure Bar(Baz: TByteSet); overload;
  end;

implementation

procedure TFoo.Bar(Baz: String);
begin
  // Do nothing
end;


procedure TFoo.Bar(Baz: TByteSet);
begin
  // Do nothing
end;

procedure Test(Foo: TFoo);
var
  Arg1: Integer;
  Arg2: String;
  Arg3: set of Byte;
begin
  Foo.Bar(Arg1);
  Foo.Bar(Arg2);
  Foo.Bar(Arg3);
end;

end.