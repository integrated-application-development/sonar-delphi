unit Distance;

{This is a sample Delphi file.}

interface

uses 
  DistantFoo;

procedure Foo; overload;

type
  TFoo = class
    constructor Create(Baz: Integer = 1); overload;
    procedure Foo(Baz: Integer = 1); overload;
  end;

  TBar = class (TFoo)
    constructor Create; overload;
    procedure Foo; overload;
    procedure Test;
  end;

implementation

procedure Foo;
begin
  // Do nothing
end;

constructor TFoo.Create(Baz: Integer); overload;
begin
  // Do nothing
end;

procedure TFoo.Foo(Baz: Integer); overload;
begin
  // Do nothing
end;

constructor TBar.Create; overload;
begin
  // Do nothing
end;

procedure TBar.Foo; overload;
begin
  // Do nothing
end;

procedure Test(Argument: Integer);
var
  Bar: TBar;
begin
  Foo;
  Foo(Argument);
  Bar := TBar.Create;
  Bar := TBar.Create(Argument);
  Bar.Foo;
  Bar.Foo(Argument);
end;

procedure TBar.Test;
begin
  Foo;
  Foo(123);
end;

end.