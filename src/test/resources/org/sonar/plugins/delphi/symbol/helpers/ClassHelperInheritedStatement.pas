unit ClassHelperInheritedStatement;

{This is a sample Delphi file.}

interface

type
  TBaseFoo = class
    procedure Bar; overload;
    procedure Bar(Baz: String); overload;
    procedure Bop(Boo: Integer);
  end;

  TFoo = class (TBaseFoo)
    procedure Bar; overload;
    procedure Bar(Baz: String); overload;
  end;

  TBaseFooHelper = class helper for TFoo
    procedure Bar; overload;
    procedure Bar(Baz: String); overload;
  end;

  TFooHelper = class helper (TBaseFooHelper) for TFoo
    procedure Bar; overload;
    procedure Bar(Baz: String); overload;
  end;

implementation

procedure TBaseFoo.Bar;
begin
  // Do nothing
end;

procedure TBaseFoo.Bar(Baz: String);
begin
  // Do nothing
end;

procedure TBaseFoo.Bop(Boo: Integer);
begin
  // Do nothing
end;

procedure TFoo.Bar;
begin
  // Do nothing
end;

procedure TFoo.Bar(Baz: String);
begin
  // Do nothing
end;

procedure TBaseFooHelper.Bar;
begin
  // Do nothing
end;

procedure TBaseFooHelper.Bar(Baz: String);
begin
  // Do nothing
end;

procedure TFooHelper.Bar;
begin
  inherited;
  inherited Bar;
  inherited Bar('Baz');
  inherited Bop(123);
end;

procedure TFooHelper.Bar(Baz: String);
begin
  inherited;
  inherited Bar;
  inherited Bar('Baz');
  inherited Bop(123);
end;

end.