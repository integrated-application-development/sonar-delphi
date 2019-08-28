unit UsesDeclarations;

{This is a sample Delphi file.}

interface

uses
  Unit1;

const
  C_Foo = 'Bar';
implementation

uses
  Unit2;

procedure Proc(Argument: String);
begin
  // Do nothing
end;

procedure Test;
begin
  Proc(C_Foo);
  Proc(UsesDeclarations.C_Foo);
  Unit1; // NOTE: Not valid Delphi code, we're only checking to see if the import are recognized.
  Unit2;
end;

end.