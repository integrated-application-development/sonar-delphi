unit PascalResultAssignments;

{This is a sample Delphi file.}

interface

function Foo(A: TObject): Integer; overload;
function Foo(A, B: TObject): Integer; overload;
function Foo(A, B, C: TObject): Integer; overload;

implementation

function Foo(A: TObject): Integer; overload;
begin
  Foo := 1;
end;

function Foo(A, B: TObject): Integer; overload;

  function NestedFoo: Integer;
  begin
    NestedFoo := 1;
    Foo := 1;
  end;

begin
  Foo := 1;
end;

function Foo(A, B, C: TObject): Integer; overload;
var
  Foo: Integer;
begin
  Foo := 1;
  Result := 1;
end;

end.